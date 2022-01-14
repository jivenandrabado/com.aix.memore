package com.aix.memore.utilities;

public class GlobalFunctions {

    public static String convertFileSize(Long fileSizeInByte){
        double fileSizeInKB = fileSizeInByte.doubleValue() / 1024;
        String conversion = "B";
        for (int i = 0; i <= 3; i ++) {
            if( roundUp(fileSizeInKB) >= 1024 ){
                fileSizeInKB = fileSizeInKB / 1024;
            }else {
                switch (i) {
                    case 1: conversion = "KB";
                        break;
                    case 2: conversion = "MB";
                        break;
                    case 3: conversion = "GB";
                        break;
                    default: conversion = "B";
                        break;
                }
                break;
            }
        }
        return roundUp(fileSizeInKB) + "" + conversion;
    }

    public static double roundUp(double number) {
        int cifras = (int) Math.pow(10, 2);
        return Math.ceil(number * cifras) / cifras;
    }
}
