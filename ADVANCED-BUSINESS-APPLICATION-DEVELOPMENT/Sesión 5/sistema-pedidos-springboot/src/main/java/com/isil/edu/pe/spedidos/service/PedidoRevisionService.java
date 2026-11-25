package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.Pedido;
import java.util.List;

public interface PedidoRevisionService {

  List<Pedido> listarPendientes();

  void revisarPedido(long pedidoId, String resultado, String observacion, String revisadoPor);
}
