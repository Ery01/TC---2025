# Analizador Léxico con Expresiones Regulares

Este repositorio contiene tres ejercicios relacionados con el análisis léxico y validación de patrones mediante expresiones regulares:

1. Implementación de un analizador léxico en JavaScript
2. Colección de expresiones regulares para validación de formatos comunes
3. Identificación y clasificación de tokens en código fuente

## 🛠️ Ejercicio 1: Implementación del Analizador Léxico

## Consigna Original

**Instrucciones:**
Implementar un analizador léxico que:
1. Analiza el fragmento de código proporcionado línea por línea
2. Identifica cada token individual (la unidad más pequeña con significado)
3. Clasifica cada token según su tipo
4. Registra la posición exacta (línea y columna) donde aparece cada token


**Fragmento de código a analizar:**
```
int suma = 10 + 5;
if (suma > 10) {
    print("El resultado es mayor que 10");
}
```

---

## 🛠️ Ejercicio 2: Expresiones Regulares

### Consigna Original

Definir expresiones regulares que permitan validar formatos comunes como direcciones de correo electrónico, números telefónicos, fechas, etc.

---

## 🛠️ Ejercicio 3: Analizador Léxico en JavaScript

### Consigna Original

Implementar en código el ejercicio 1.
1. El objetivo es crear un analizador léxico que procese un fragmento de código fuente, identifique los tokens, y los clasifique por tipo.
2. El programa debe indicar la línea y columna de aparición de cada token.
3. Puede implementarse en el lenguaje de tu preferencia (JavaScript).
4. El resultado debe ser una tabla o estructura de datos que muestre los campos: Token, Tipo, Línea, Columna.
