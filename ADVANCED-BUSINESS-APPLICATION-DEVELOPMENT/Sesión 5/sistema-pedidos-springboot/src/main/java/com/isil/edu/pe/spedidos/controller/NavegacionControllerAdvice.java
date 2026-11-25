package com.isil.edu.pe.spedidos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavegacionControllerAdvice {

  @ModelAttribute("puedeSupervisar")
  public boolean puedeSupervisar(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_OPERADOR".equals(authority.getAuthority()));
  }

  @ModelAttribute("paginaActiva")
  public String paginaActiva(HttpServletRequest request) {
    String ruta = request.getRequestURI().substring(request.getContextPath().length());
    if (ruta.startsWith("/supervision/productos")) {
      return "supervision-productos";
    }
    if (ruta.startsWith("/supervision/pedidos")) {
      return "supervision-pedidos";
    }
    if (ruta.startsWith("/reportes/pedidos")) {
      return "reportes";
    }
    return "pedidos";
  }
}
