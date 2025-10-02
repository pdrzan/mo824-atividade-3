# Heurística de Busca Tabu para o problema MAX_SC_QBF

[![Static Badge](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/)

Esse repositório contém a implementação da Heurítica de Tabu Search para o problema MAX_SC_QBF.

## Organização do repositório

Esse repositório é organizado da seguinte forma:
- `/src`: Possui o código fonte da heurística.
- `/instances`: Possui um conjunto de instâncias do problema.
- `/results`: Possuem o resultado da execução da heurística para instâncias. Mais detalhes sobre sua execução podem ser encontrados <a href="results/README.md"> aqui <a/>.

## Descrição do problema

O problema MAX-SC-QBF consiste na maximização de uma Função Binária Quadrática (QBF) sujeita a um conjunto de restrições de Cobertura de Conjuntos (Set Covering). Seja $N = \{ 1, \dots, n\}$ o conjunto de variáveis da QBF. Seja $S = \{ S_1, \dots, S_n \}$, uma coleção de subconjuntos $S_{i} \subseteq N$ que representa as variáveis que o subconjuto $i$ cobre. Para cada par de subconjuntos $(i, j)$, existe um coeficiente $a_{ij} \in \mathbb{R}$ (positivo ou negativo) que representa o ganho de selecionar os conjuntos $S_{i}$ e $S_{j}$ simultaneamente. É possível representar esses coeficientes por meio de uma matriz $A_{n \times n}$ triangular superior, onde cada entrada corresponde a um dado $a_{ij}$.

Para modelar o problema, definimos as seguinte variáveis binárias:
```
\begin{itemize}
    \item $x_i = \begin{cases} 
            1, & \text{se o conjunto $S_{i}$ foi selecionado}\\
            0, & \text{caso contrário}
            \end{cases}$

    \item $y_{ij} = \begin{cases} 
            1, & \text{se $x_i=1$ e $x_j=1$}\\
            0, & \text{caso contrário}
            \end{cases}$
\end{itemize}
```


Considerando essas variáveis, é possível modelar o problema da seuinte forma:
```
\begin{align}
    \text{(MAX-SC-QBF)} \quad 
        & \max \sum_{i \in N}\sum_{j \in N} \text{a}_{ij} y_{ij} 
        \label{eq:OF} \\[0.5em]
    \text{s.t.}\quad 
        & \sum_{i : k \in S_{i}} x_{i} \geqslant 1 
        & \forall k \in N \label{eq:varcoverage1} \\
        & y_{ij}  \leqslant x_{i} 
        & \forall i,j \in N \label{eq:varcoverage2} \\
        & y_{ij} \leqslant x_{j} 
        & \forall i,j \in N \label{eq:varcoverage3} \\
        & y_{ij}  \geqslant x_{i} + x_{j} - 1 
        & \forall i,j \in N \label{eq:varcoverage4} \\[0.5em]
        & x_{i} \in \{0,1\} 
        & \forall i \in N \label{eq:domains1} \\
        & y_{ij} \in \{0,1\} 
        & \forall i \in N, j \in N\label{eq:domains2} 
\end{align}
```

A função objetivo, dada pela equação `~\eqref{eq:OF}`, busca maximizar o somatório ponderado das variáveis $y_{ij}$, cujos coeficientes $a_{ij} \in \mathbb{R}$ representam o ganho ou custo associado à seleção simultânea dos conjuntos $S_{i}$ e $S_{j}$. Dessa forma, a solução escolhida procura maximizar a contribuição total definida por uma matriz de coeficientes $A$ recebida como entrada.

As restrições de cobertura, expressas pelo conjunto de restrições `~\eqref{eq:varcoverage1}`, garantem que cada elemento $k \in N$ seja coberto por pelo menos um subconjunto $S_i$. Assim, assegura-se que todos os elementos do universo estejam contemplados na solução.

As restrições `~\eqref{eq:varcoverage2}--\eqref{eq:varcoverage4}` realizam a linearização do produto de variáveis binárias, impondo que $y_{ij}$ assuma valor $1$ se, e somente se, ambas as variáveis $x_i$ e $x_j$ forem iguais a $1$. Essa linearização elimina a não linearidade do problema original, permitindo que o modelo seja resolvido por métodos de programação linear inteira.

Por fim, as restrições de domínio `\eqref{eq:domains1}` e `\eqref{eq:domains2}` estabelecem que as variáveis $x_i$ e $y_{ij}$ são binárias.
