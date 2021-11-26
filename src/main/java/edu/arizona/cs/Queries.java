package edu.arizona.cs;


class Queries {
    private String clue = "";
    private String question = "";
    private String answer = "";

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuery(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Query{" +
                "category='" + clue + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}

