package com.takapay.smsforwarder.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    public static class ParsedSms {
        public String provider;
        public String trxId;
        public String amount;
        public String sender;
        public String type;
        public boolean isValid;
    }

    public static ParsedSms parse(String sender, String body) {
        ParsedSms result = new ParsedSms();
        result.isValid = false;

        String bodyLower = body.toLowerCase();

        if (sender.toLowerCase().contains("bkash") || bodyLower.contains("bkash")) {
            result.provider = "bkash";
            result = parseBkash(body, result);
        }
        else if (sender.toLowerCase().contains("nagad") || bodyLower.contains("nagad")) {
            result.provider = "nagad";
            result = parseNagad(body, result);
        }
        else if (sender.toLowerCase().contains("rocket") || sender.toLowerCase().contains("dbbl") || bodyLower.contains("rocket")) {
            result.provider = "rocket";
            result = parseRocket(body, result);
        }
        else if (sender.toLowerCase().contains("upay") || bodyLower.contains("upay")) {
            result.provider = "upay";
            result = parseUpay(body, result);
        }

        return result;
    }

    private static ParsedSms parseBkash(String body, ParsedSms result) {
        Pattern amountPattern = Pattern.compile("(?:Tk|TK|tk)\\s*([\\d,]+(?:\\.\\d{1,2})?)");
        Matcher amountMatcher = amountPattern.matcher(body);
        if (amountMatcher.find()) {
            result.amount = amountMatcher.group(1).replace(",", "");
        }

        Pattern trxPattern = Pattern.compile("TrxID\\s*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher trxMatcher = trxPattern.matcher(body);
        if (trxMatcher.find()) {
            result.trxId = trxMatcher.group(1);
        }

        String bodyLower = body.toLowerCase();
        if (bodyLower.contains("received")) {
            result.type = "received";
        } else if (bodyLower.contains("sent")) {
            result.type = "sent";
        } else if (bodyLower.contains("cash out")) {
            result.type = "cashout";
        } else if (bodyLower.contains("payment")) {
            result.type = "payment";
        } else if (bodyLower.contains("add money")) {
            result.type = "topup";
        }

        result.isValid = (result.amount != null && result.trxId != null);
        return result;
    }

    private static ParsedSms parseNagad(String body, ParsedSms result) {
        Pattern amountPattern = Pattern.compile("(?:BDT|Tk|TK)\\s*([\\d,]+(?:\\.\\d{1,2})?)");
        Matcher amountMatcher = amountPattern.matcher(body);
        if (amountMatcher.find()) {
            result.amount = amountMatcher.group(1).replace(",", "");
        }

        if (result.amount == null) {
            Pattern banglaAmountPattern = Pattern.compile("([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:টাকা|taka)", Pattern.CASE_INSENSITIVE);
            Matcher m = banglaAmountPattern.matcher(body);
            if (m.find()) {
                result.amount = m.group(1).replace(",", "");
            }
        }

        Pattern trxPattern = Pattern.compile("(?:TxnID|Transaction ID|ট্রানজেকশন আইডি)[:\\s]*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher trxMatcher = trxPattern.matcher(body);
        if (trxMatcher.find()) {
            result.trxId = trxMatcher.group(1);
        }

        String bodyLower
