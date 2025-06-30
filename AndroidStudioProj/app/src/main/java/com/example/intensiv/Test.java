package com.example.intensiv;

import java.util.List;

public class Test {
        private int id;
        private String title;
        private String description;
        private int completion_percentage;
        private String background_image;
        private String status_image;
        private List<Question> questions;

        // Геттеры и сеттеры
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getCompletionPercentage() {
            return completion_percentage;
        }

        public void setCompletionPercentage(int completion_percentage) {
            this.completion_percentage = completion_percentage;
        }

        public String getBackgroundImage() {
            return background_image;
        }

        public void setBackgroundImage(String background_image) {
            this.background_image = background_image;
        }

        public String getStatusImage() {
            return status_image;
        }

        public void setStatusImage(String status_image) {
            this.status_image = status_image;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }
    }
