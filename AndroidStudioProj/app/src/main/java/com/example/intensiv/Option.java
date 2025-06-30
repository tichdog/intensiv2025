package com.example.intensiv;

public class Option {
        private int id;
        private String text;
        private boolean is_correct;

        // Геттеры и сеттеры
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isCorrect() {
            return is_correct;
        }

        public void setCorrect(boolean is_correct) {
            this.is_correct = is_correct;
        }
    }
