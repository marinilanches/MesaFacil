package com.example.mesafacil.utils

import com.example.mesafacil.data.models.Pagamento
import com.example.mesafacil.data.models.Pedido
import java.text.SimpleDateFormat
import java.util.Date

object PrinterManager {

    fun gerarComandaPedido(pedido: Pedido): String {
        val sb = StringBuilder()

        sb.append("=================================\n")
        sb.append("         COMANDA DO PEDIDO       \n")
        sb.append("=================================\n\n")

        sb.append("Mesa: ${pedido.numeroMesa}\n")
        sb.append("Garçom: ${pedido.garcomNome}\n")
        sb.append("Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(pedido.createdAt))}\n")
        sb.append("---------------------------------\n\n")

        pedido.itens.forEach { item ->
            sb.append("${item.quantidade}x ${item.nome}\n")

            if (item.adicionais.isNotEmpty()) {
                item.adicionais.forEach { adicional ->
                    sb.append("  + $adicional\n")
                }
            }

            if (item.observacoes.isNotEmpty()) {
                sb.append("  Obs: ${item.observacoes}\n")
            }

            sb.append("\n")
        }

        if (pedido.observacoes.isNotEmpty()) {
            sb.append("---------------------------------\n")
            sb.append("Observações: ${pedido.observacoes}\n")
        }

        sb.append("---------------------------------\n")
        sb.append("TOTAL: R$ ${String.format("%.2f", pedido.valorTotal)}\n")
        sb.append("=================================\n")

        return sb.toString()
    }

    fun gerarReciboPagamento(pagamento: Pagamento): String {
        val sb = StringBuilder()

        sb.append("=================================\n")
        sb.append("         RECIBO DO CLIENTE      \n")
        sb.append("=================================\n\n")

        sb.append("Mesa: ${pagamento.numeroMesa}\n")
        sb.append("Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(pagamento.createdAt))}\n")
        sb.append("---------------------------------\n\n")

        sb.append("Valor Total: R$ ${String.format("%.2f", pagamento.valorTotal)}\n")
        sb.append("Pessoas: ${pagamento.quantidadePessoas}\n")
        sb.append("Pagantes: ${pagamento.quantidadePagantes}\n")
        sb.append("Por Pessoa: R$ ${String.format("%.2f", pagamento.valorPorPessoa())}\n\n")

        sb.append("---------------------------------\n")
        sb.append("FORMAS DE PAGAMENTO:\n")

        pagamento.pagamentos.forEach { p ->
            sb.append("${p.formaPagamento}: R$ ${String.format("%.2f", p.valor)}\n")
        }

        if (pagamento.troco > 0) {
            sb.append("---------------------------------\n")
            sb.append("TROCO: R$ ${String.format("%.2f", pagamento.troco)}\n")
        }

        sb.append("=================================\n")
        sb.append("     Obrigado pela preferência!  \n")
        sb.append("=================================\n")

        return sb.toString()
    }

    fun printComanda(pedido: Pedido): Boolean {
        return try {
            val comanda = gerarComandaPedido(pedido)
            println("[IMPRESSO] Comanda do pedido ${pedido.id}")
            println(comanda)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun printRecibo(pagamento: Pagamento): Boolean {
        return try {
            val recibo = gerarReciboPagamento(pagamento)
            println("[IMPRESSO] Recibo do pagamento ${pagamento.id}")
            println(recibo)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}