# SentinelAC

Anticheat para **Paper 1.21.11** (Java 21), com leitura de pacotes via **PacketEvents 2.11.2** (embutido no jar).

## Compilar

```bash
gradle build          # ou ./gradlew build se você gerar o wrapper (gradle wrapper)
```

O jar final fica em `build/libs/SentinelAC-0.1.0.jar`. Coloque em `plugins/` e reinicie o servidor.
Não precisa instalar o PacketEvents separado (ele é embutido e relocado).

## Checks incluídos

| Categoria | Check | O que detecta |
|---|---|---|
| Movimento | SpeedA | Velocidade horizontal acima do limite (usa o atributo de movimento: sprint, Speed, etc.) |
| Movimento | FlyA | Flutuar no ar por vários ticks |
| Movimento | FlyB | Gravidade incorreta (dy não segue `(dy_ant - 0.08) * 0.98`) |
| Movimento | NoFallA | Cliente diz `onGround=true` sem ter chão |
| Combate | ReachA | Alcance maior que o permitido (considera ping) |
| Combate | KillAuraA | Ataque sem animação de braço |
| Combate | KillAuraB | Vários alvos no mesmo tick |
| Combate | KillAuraC | Acertar alguém fora do campo de visão |
| Combate | AutoClickerA | CPS acima do limite |
| Combate | AutoClickerB | Cliques regulares demais |
| Pacotes | BadPacketsA | Pitch/yaw/posição impossíveis |
| Pacotes | TimerA | Pacotes de movimento rápido demais |
| Mineração | FastBreakA | Quebrar bloco mais rápido que a ferramenta permite |
| Mineração | NukerA | Blocos/segundo alto ou vários blocos no mesmo tick |
| Mineração | NukerB | Muitos minérios em poucos segundos |
| Mineração | NukerC | Quebrar bloco longe demais |

## Comandos e permissões

- `/sentinel alerts` – liga/desliga seus alertas
- `/sentinel vl <jogador>` – mostra as violações
- `/sentinel reload` – recarrega `config.yml`
- `sentinel.admin`, `sentinel.alerts`, `sentinel.bypass`

## Como criar um novo check

1. Crie uma classe em `check/impl/` estendendo `Check` e sobrescreva o hook que precisa
   (`onMove`, `onFlyingPacket`, `onAttack`, `onBlockBreak`...).
2. Chame `flag("detalhes")` quando detectar algo.
3. Registre em `CheckRegistry` e adicione as opções em `config.yml` (`checks.NomeA`).

## Regras de thread (importante)

- `onFlyingPacket`, `onAnimationPacket`, `onAttackPacket` rodam na **thread de rede**: use só dados do próprio check, nunca a API do Bukkit (mundo, blocos, entidades).
- Os demais hooks rodam na **thread principal** e podem usar o Bukkit livremente.

## Limitações conhecidas

- Não é um motor de predição como o Vulcan/Grim: os checks de movimento são heurísticos (limites + buffer). Ajuste os valores no `config.yml` com jogadores reais.
- Possíveis falsos positivos: wind charge, mace, tridente, elytra com foguete, blocos de gelo/slime, efeitos de outros plugins. Teste esses casos e ajuste exceções.
- TimerA: no 1.21.x o cliente não envia pacote parado, então o "banco" de lag é limitado por `max-lag-bank-ms`.
- Plugins que quebram vários blocos de uma vez (vein miner, tree capitator) podem acionar o NukerA; dê `sentinel.bypass` ou desative o check.
- Paper apenas (Folia exige agendadores próprios).
- Este código não foi compilado nem testado em servidor ainda.
