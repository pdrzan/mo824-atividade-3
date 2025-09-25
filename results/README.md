# Resultados dos testes

Um conjunto de instâncias presente em `instances/max_sc_qbf` foram utilizadas para executar a heurística de Tabu Search. Cada subdiretório desse diretório apresenta os resultados das seguintes configurações:

- Configuração 1: Ternure de 10 + Estratégia de *best-improvement*
- Configuração 2: Ternure de 10 + Estratégia de *first-improvement*
- Configuração 3: Ternure de 20 + Estratégia de *first-improvement*
- Configuração 4: *Probabilistic TS* + $\alpha = 0.8$ + Ternure de 10 + Estratégia de *first-improvement*
- Configuração 5: *Intensification by Neighborhood* + $\beta = 4$ + Ternure de 10 + Estratégia de *first-improvement*
- Configuração 6: *Probabilistic TS* + *Intensification by Neighborhood* + $\alpha = 0.8$ + $\beta = 4$ + Ternure de 10 + Estratégia de *first-improvement*
- Configuração 7: Ternure de 40 + Estratégia de *first-improvement*

Todos os testes foram executados com um limite de tempo de 30 minutos.