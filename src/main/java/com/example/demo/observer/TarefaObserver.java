package com.example.demo.observer;

import com.example.demo.model.Tarefa;

public interface TarefaObserver {

    void onTarefaCriada(Tarefa tarefa);

    void onTarefaExcluida(Tarefa tarefa);
}
