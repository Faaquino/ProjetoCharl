package org.apache.maven.archetypes;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Criar um Scanner para capturar a entrada do usuário
        Scanner scanner = new Scanner(System.in);

        // Solicitar ao usuário que insira uma parte do nome do arquivo para a pesquisa
        System.out.print("Digite parte do nome do arquivo para pesquisa: ");
        String query = scanner.nextLine();
        System.out.println("Consulta recebida: " + query); // Mensagem de depuração

        // Construir a URL de pesquisa
        String url = "https://gutendex.com/books/?search=" + query;
        System.out.println("URL construída: " + url); // Mensagem de depuração

        // Criar um cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Construir a solicitação HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            // Enviar a solicitação e obter a resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Imprimir o status da resposta
            System.out.println("Status da resposta: " + response.statusCode());

            // Imprimir os cabeçalhos da resposta
            System.out.println("Cabeçalhos da resposta: " + response.headers());

            // Imprimir o corpo da resposta
            String responseBody = response.body();
            System.out.println("Resposta recebida: " + responseBody);

            // Extrair títulos, autores, subjects, id e birth_year
            extractAndSaveBookDetails(responseBody);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void extractAndSaveBookDetails(String responseBody) {
        try (FileWriter writer = new FileWriter("resultados.txt")) {
            String[] books = responseBody.split("\\{\"id\":");
            for (String book : books) {
                if (book.contains("\"title\":") && book.contains("\"authors\":")) {
                    String id = extractValue(book, "", ",");
                    String title = extractValue(book, "\"title\":\"", "\",");
                    String author = extractValue(book, "\"name\":\"", "\",");
                    String birthYear = extractValue(book, "\"birth_year\":", ",");
                    String subjects = extractSubjects(book);

                    writer.write("ID: " + id + "\n");
                    writer.write("Title: " + title + "\n");
                    writer.write("Author: " + author + "\n");
                    writer.write("Birth Year: " + birthYear + "\n");
                    writer.write("Subjects: " + subjects + "\n\n");
                }
            }
            System.out.println("Dados salvos em resultados.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractValue(String text, String startDelimiter, String endDelimiter) {
        int startIndex = text.indexOf(startDelimiter) + startDelimiter.length();
        int endIndex = text.indexOf(endDelimiter, startIndex);
        if (startIndex >= startDelimiter.length() && endIndex > startIndex) {
            return text.substring(startIndex, endIndex).replace("\\\"", "\"");
        }
        return "N/A";
    }

    private static String extractSubjects(String book) {
        StringBuilder subjects = new StringBuilder();
        int subjectsIndex = book.indexOf("\"subjects\":[");
        if (subjectsIndex != -1) {
            int startIndex = subjectsIndex + "\"subjects\":[".length();
            int endIndex = book.indexOf("]", startIndex);
            String subjectsArray = book.substring(startIndex, endIndex);
            String[] subjectsList = subjectsArray.split(",");
            for (String subject : subjectsList) {
                subjects.append(subject.replace("\"", "").trim()).append(", ");
            }
            if (subjects.length() > 2) {
                subjects.setLength(subjects.length() - 2); // Remove a última vírgula e espaço
            }
        }
        return subjects.toString();
    }
}