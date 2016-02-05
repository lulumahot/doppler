package com.example.lulu.doppler.listeners;

public abstract class OnSoundReadListener {
    public abstract void OnReceive(short[] buffer, int nbRealValues);
}
