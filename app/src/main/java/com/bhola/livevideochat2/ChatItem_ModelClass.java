package com.bhola.livevideochat2;

import java.util.ArrayList;


public class ChatItem_ModelClass {
    private int id;
    private String userName;
    private String gender;
    private String age;
    private String country;
    private  ArrayList<String> contentImages;
    private String users;
    private String answerRate;
    private String userProfile;
    private boolean containsQuestion;
    private String recommendationType;
    private ArrayList<UserBotMsg> userBotMsg;
    private UserQuestionWithAns questionWithAns;

    public ChatItem_ModelClass() {
    }

    public ChatItem_ModelClass(int id, String userName, String gender, String age, String country, ArrayList<String> contentImages, String users, String answerRate, String userProfile, boolean containsQuestion, String recommendationType, ArrayList<UserBotMsg> userBotMsg, UserQuestionWithAns questionWithAns) {
        this.id = id;
        this.userName = userName;
        this.gender = gender;
        this.age = age;
        this.country = country;
        this.contentImages = contentImages;
        this.users = users;
        this.answerRate = answerRate;
        this.userProfile = userProfile;
        this.containsQuestion = containsQuestion;
        this.recommendationType = recommendationType;
        this.userBotMsg = userBotMsg;
        this.questionWithAns = questionWithAns;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ArrayList<String> getContentImages() {
        return contentImages;
    }

    public void setContentImages(ArrayList<String> contentImages) {
        this.contentImages = contentImages;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getAnswerRate() {
        return answerRate;
    }

    public void setAnswerRate(String answerRate) {
        this.answerRate = answerRate;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public boolean isContainsQuestion() {
        return containsQuestion;
    }

    public void setContainsQuestion(boolean containsQuestion) {
        this.containsQuestion = containsQuestion;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public ArrayList<UserBotMsg> getUserBotMsg() {
        return userBotMsg;
    }

    public void setUserBotMsg(ArrayList<UserBotMsg> userBotMsg) {
        this.userBotMsg = userBotMsg;
    }

    public UserQuestionWithAns getQuestionWithAns() {
        return questionWithAns;
    }

    public void setQuestionWithAns(UserQuestionWithAns questionWithAns) {
        this.questionWithAns = questionWithAns;
    }
}

