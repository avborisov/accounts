package ru.smitdev.accounts.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class NamesGenerator {

    private Random rand = new Random();
    private String[] menNames;
    private String[] menOtec;
    private String[] menFam;
    private String[] womenNames;
    private String[] womenOtec;
    private String[] womenFam;

    public NamesGenerator() {
        try {
            readFiles();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String generateManName() {
        StringBuilder sb = new StringBuilder();
        sb.append(menNames[rand.nextInt(menNames.length)]).append(" ")
                .append(menOtec[rand.nextInt(menOtec.length)]).append(" ")
                .append(menFam[rand.nextInt(menFam.length)]).append(" ");
        return sb.toString();
    }

    public String generateWomanName() {
        StringBuilder sb = new StringBuilder();
        sb.append(womenNames[rand.nextInt(womenNames.length)]).append(" ")
                .append(womenOtec[rand.nextInt(womenOtec.length)]).append(" ")
                .append(womenFam[rand.nextInt(womenFam.length)]).append(" ");
        return sb.toString();
    }

    private void readFiles() throws FileNotFoundException {
        menNames = getFile("baseMenName.txt");
        menOtec = getFile("baseMenOtec.txt");
        menFam = getFile("baseMenFam.txt");

        womenNames = getFile("baseWomenName.txt");
        womenOtec = getFile("baseWomenOtec.txt");
        womenFam = getFile("baseWomenFam.txt");
    }

    private String[] getFile(String fileName) throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:names/" + fileName);
        ArrayList<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file,"UTF-8")) {
            scanner.useDelimiter("[;\r\n]+");
            while (scanner.hasNext()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.toArray(new String[0]);

    }

}
