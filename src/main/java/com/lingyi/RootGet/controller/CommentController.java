package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Comment;
import com.lingyi.RootGet.entry.Danmaku;
import com.lingyi.RootGet.mapper.video.CommentMapper;
import com.lingyi.RootGet.tools.Constant;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentMapper commentMapper;

    public CommentController(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @GetMapping("/getComments/{id}")
    public List<Comment> getComments(@PathVariable("id") long id) {
        List<Comment> comments = commentMapper.getCommentsByForId(id);
        for (Comment comment : comments) {
            List<Comment> list = commentMapper.getCommentsByForId(comment.getId());
            comment.setCommentList(list);
        }
        return comments;
    }

    @PostMapping("/addComment")
    public String addComment(@RequestBody Comment comment) {
        ArrayList<String> forbiddenWords = Constant.getForbiddenWords();
        for (String forbiddenWord : forbiddenWords) {
            if (comment.getText().contains(forbiddenWord))
                return Constant.WordsForbidden;
        }
        int result = commentMapper.addComment(comment);
        if (result != 1) return Constant.FAILURE;
        return Constant.SUCCESS;
    }

    @PostMapping("/deleteComment")
    public String deleteComment(@RequestBody Comment comment, HttpServletRequest request) {
        String uid = Constant.getUidInCookie(request);
        if (uid == null || comment.getUid() != Long.parseLong(uid)) return Constant.NoPermission;
        int result = commentMapper.deleteComment(comment);
        if (result != 1) return Constant.FAILURE;
        return Constant.SUCCESS;
    }
    @GetMapping("/getDanmaku/v3")
    public Danmaku get(@RequestParam("id")long id, HttpServletResponse response) throws ParserConfigurationException, IOException, SAXException {
        Danmaku danmaku = new Danmaku();
        List<List<String>> data = new ArrayList<>();
        File file = new File("/opt/Server/videoStation/danmaku/"+id+".xml");
        if(!file.exists()){
            response.setStatus(503);
            return null;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        NodeList nodeList = document.getElementsByTagName("d");
        for(int i=0;i< nodeList.getLength();i++){
            Node node = nodeList.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node p = attributes.getNamedItem("p");
            String value = p.getNodeValue();
            String[] strings = value.split(",");
            List<String> list = new ArrayList<>();
            list.add(strings[0]);
            list.add(strings[1]);
            list.add(Integer.toHexString(Integer.parseInt(strings[3])));
            list.add(strings[6]);
            String content = node.getTextContent();
            list.add(content);
            data.add(list);
        }
        danmaku.setData(data);
        danmaku.setCode(0);
        return danmaku;
    }
}
