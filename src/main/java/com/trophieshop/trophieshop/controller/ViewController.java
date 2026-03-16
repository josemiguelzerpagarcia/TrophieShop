package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.repository.VideojuegoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Controller
public class ViewController {

    private final VideojuegoRepository videojuegoRepository;

    public ViewController(VideojuegoRepository videojuegoRepository) {
        this.videojuegoRepository = videojuegoRepository;
    }

    @GetMapping("/videojuegos")
    @ResponseBody
    public String videojuegos() {
        List<Videojuego> videojuegos = videojuegoRepository.findAll();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Videojuegos - TrophieShop</title>");
        html.append("<style>");
        html.append("body{font-family:Segoe UI,Tahoma,Geneva,Verdana,sans-serif;background:#f6f8fb;margin:0;color:#1f2937;}");
        html.append(".container{max-width:900px;margin:40px auto;background:#fff;border-radius:10px;box-shadow:0 8px 24px rgba(0,0,0,.08);padding:24px;}");
        html.append("h1{margin-top:0;font-size:1.8rem;} .meta{color:#4b5563;margin-bottom:16px;}");
        html.append("table{width:100%;border-collapse:collapse;} th,td{text-align:left;padding:10px;border-bottom:1px solid #e5e7eb;} th{background:#f3f4f6;}");
        html.append(".empty{padding:18px;background:#fff8e1;border:1px solid #fde68a;border-radius:8px;color:#92400e;}");
        html.append("</style></head><body><div class=\"container\">");
        html.append("<h1>Listado de Videojuegos</h1>");
        html.append("<p class=\"meta\">Total registrados: <strong>").append(videojuegos.size()).append("</strong></p>");

        if (videojuegos.isEmpty()) {
            html.append("<div class=\"empty\">No hay videojuegos en la base de datos.</div>");
        } else {
            html.append("<table><thead><tr><th>ID</th><th>Titulo</th></tr></thead><tbody>");
            for (Videojuego videojuego : videojuegos) {
                html.append("<tr><td>")
                        .append(videojuego.getId())
                        .append("</td><td>")
                        .append(HtmlUtils.htmlEscape(videojuego.getTitulo()))
                        .append("</td></tr>");
            }
            html.append("</tbody></table>");
        }

        html.append("</div></body></html>");
        return html.toString();
    }
}
