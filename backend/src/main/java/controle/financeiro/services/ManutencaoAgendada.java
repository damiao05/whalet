package controle.financeiro.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManutencaoAgendada {

    private final RecorrenciaService recorrenciaService;

    /** Ao iniciar: recupera o que ficou para trás se o servidor estava desligado às 3h. */
    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        executar();
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Sao_Paulo")
    public void diaria() {
        executar();
    }

    private void executar() {
        try {
            recorrenciaService.manutencao();
            log.info("Manutenção de recorrências concluída");
        } catch (Exception e) {
            log.error("Falha na manutenção de recorrências", e);
        }
    }
}