package com.bj4.yhh.accountant.service;

interface ParseServiceCallback {
 void loadingProcess(int percentage);
 void loadingDone();
 void startLoading();
}