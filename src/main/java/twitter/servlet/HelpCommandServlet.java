package twitter.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HelpCommandServlet extends HttpServlet {

    private String yamlCache; // Кэшируем YAML-файл в памяти

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Убедись, что твой файл в /resources называется ИМЕННО 'openapi.yaml'
        String fileName = "openapi.yaml";

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {

            if (inputStream == null) {
                // Если файл не найден, кэшируем ошибку
                this.yamlCache = "ОШИБКА: Не могу найти файл '" + fileName + "' в /resources.";
                System.err.println(this.yamlCache); // Выводим в лог
                return;
            }

            // Читаем файл в одну большую строку
            StringBuilder textBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    textBuilder.append(line).append(System.lineSeparator());
                }
            }
            this.yamlCache = textBuilder.toString();
            System.out.println("Файл " + fileName + " успешно загружен в кэш OpenApiServlet.");

        } catch (IOException e) {
            throw new ServletException("Не удалось прочитать " + fileName, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Устанавливаем правильный тип контента
        resp.setContentType("application/x-yaml; charset=utf-8");

        try (PrintWriter writer = resp.getWriter()) {
            writer.print(this.yamlCache); // Отдаем содержимое файла
        }
    }
}
