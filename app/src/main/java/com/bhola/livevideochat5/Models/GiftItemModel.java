package com.bhola.livevideochat5.Models;

public class GiftItemModel {
    private String giftName;
    private int coin;
    private boolean selected;

    public GiftItemModel() {
    }

    public GiftItemModel(String giftName, int coin, boolean selected) {
        this.giftName = giftName;
        this.coin = coin;
        this.selected = selected; // Default value is false
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}