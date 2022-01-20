package com.aix.memore.utilities;

public class GlobalFunctions {

    public static String convertFileSize(Long fileSizeInByte){
        double fileSizeInKB = fileSizeInByte;
        String conversion = "B";
        for (int i = 0; i <= 3; i ++) {
            if( roundUp(fileSizeInKB) >= 1024 ){
                fileSizeInKB = fileSizeInKB / 1024;
                switch (i) {
                    case 0: conversion = "KB";
                        break;
                    case 1: conversion = "MB";
                        break;
                    case 2: conversion = "GB";
                        break;
                    default: conversion = "B";
                        break;
                }
            }else break;
        }
        return roundUp(fileSizeInKB) + "" + conversion;
    }

    public static double roundUp(double number) {
        int cifras = (int) Math.pow(10, 2);
        return Math.ceil(number * cifras) / cifras;
    }
}
