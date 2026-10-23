package com.gestionrh.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gestionrh.sirh.service.TableauBordRhService;

@Controller
@RequestMapping("/rh/tableau-de-bord")
public class TableauBordRhController {

    private final TableauBordRhService service;

    public TableauBordRhController(TableauBordRhService service) {
        this.service = service;
    }

    @GetMapping
    public String tableau(Model model) {
        model.addAttribute("tableau", service.resume());
        return "rh/tableau-bord";
    }
}
