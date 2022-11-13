package com.lingyi.RootGet.entry;

public class Words {
    private String word,translate,character,masterpiece;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getMasterpiece() {
        return masterpiece;
    }

    public void setMasterpiece(String masterpiece) {
        this.masterpiece = masterpiece;
    }

    @Override
    public String toString() {
        return "Words{" +
                "word='" + word + '\'' +
                ", translate='" + translate + '\'' +
                ", character='" + character + '\'' +
                ", masterpiece='" + masterpiece + '\'' +
                '}';
    }
}
