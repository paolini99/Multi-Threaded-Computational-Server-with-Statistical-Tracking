package it.units.project.expression;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.units.project.expression.Node;
import it.units.project.expression.Variable;
import it.units.project.expression.Constant;
import it.units.project.expression.Operator;


public class Parser {

  // Grammatica BNF:
  //  <e> ::= <n> | <v> | (<e> <o> <e>)
  // Dove:
  // <e> è un'espressione, <n> è una costante numerica, <v> è una variabile,
  // <o> è un operatore (es. +, -, *, /), e le espressioni possono essere racchiuse tra parentesi.

  private final String string;  // La stringa di input da analizzare
  private int cursor = 0;       // Il cursore che traccia la posizione attuale nella stringa

  // Costruttore della classe Parser: rimuove gli spazi dalla stringa di input
  public Parser(String string) {
    this.string = string.replace(" ", "");  // Rimuove eventuali spazi nella stringa
  }

  // Enum che rappresenta i vari tipi di token che il parser riconosce
  public enum TokenType {
    CONSTANT("[0-9]+(\\.[0-9]+)?"),  // Costanti numeriche (interi e decimali)
    VARIABLE("[a-z][a-z0-9]*"),      // Variabili che iniziano con una lettera e possono avere cifre
    OPERATOR("[+\\-\\*/\\^]"),       // Operatori aritmetici
    OPEN_BRACKET("\\("),             // Parentesi aperta
    CLOSED_BRACKET("\\)");           // Parentesi chiusa

    private final String regex;      // L'espressione regolare associata a ciascun token

    // Costruttore dell'enum: associa un pattern regex a ciascun tipo di token
    TokenType(String regex) {
      this.regex = regex;
    }

    // Metodo che cerca il prossimo token di questo tipo nella stringa
    public Token next(String s, int i) {
      Matcher matcher = Pattern.compile(regex).matcher(s);  // Crea un matcher usando la regex del token
      if (!matcher.find(i)) {  // Se il matcher non trova un match, restituisce null
        return null;
      }
      return new Token(matcher.start(), matcher.end());  // Restituisce il token con posizione di inizio e fine
    }

    public String getRegex() {
      return regex;  // Restituisce la regex associata al token
    }
  }

  // Classe interna che rappresenta un token con il suo intervallo nella stringa (start e end)
  private static class Token {
    private final int start;  // Inizio del token
    private final int end;    // Fine del token

    public Token(int start, int end) {
      this.start = start;
      this.end = end;
    }
  }

  // Metodo principale del parser che analizza la stringa e costruisce l'albero sintattico (AST)
  public Node parse() throws IllegalArgumentException {
    Token token;

    // Prova a trovare una costante
    token = TokenType.CONSTANT.next(string, cursor);
    if (token != null && token.start == cursor) {  // Se trova una costante
      cursor = token.end;  // Sposta il cursore dopo la costante
      return new Constant(Double.parseDouble(string.substring(token.start, token.end)));  // Crea un nodo Constant
    }

    // Prova a trovare una variabile
    token = TokenType.VARIABLE.next(string, cursor);
    if (token != null && token.start == cursor) {  // Se trova una variabile
      cursor = token.end;  // Sposta il cursore dopo la variabile
      return new Variable(string.substring(token.start, token.end));  // Crea un nodo Variable
    }

    // Prova a trovare una parentesi aperta
    token = TokenType.OPEN_BRACKET.next(string, cursor);
    if (token != null && token.start == cursor) {  // Se trova una parentesi aperta
      cursor = token.end;  // Sposta il cursore dopo la parentesi

      // Analizza il primo sottoalbero (prima espressione)
      Node child1 = parse();

      // Cerca l'operatore
      Token operatorToken = TokenType.OPERATOR.next(string, cursor);
      if (operatorToken != null && operatorToken.start == cursor) {  // Se trova l'operatore
        cursor = operatorToken.end;  // Sposta il cursore dopo l'operatore
      } else {
        // Se non trova un operatore, lancia un'eccezione
        throw new IllegalArgumentException(String.format(
                "Unexpected char at %d instead of operator: '%s'",
                cursor,
                string.charAt(cursor)
        ));
      }

      // Analizza il secondo sottoalbero (seconda espressione)
      Node child2 = parse();

      // Cerca una parentesi chiusa
      Token closedBracketToken = TokenType.CLOSED_BRACKET.next(string, cursor);
      if (closedBracketToken != null && closedBracketToken.start == cursor) {  // Se trova la parentesi chiusa
        cursor = closedBracketToken.end;  // Sposta il cursore dopo la parentesi chiusa
      } else {
        // Se non trova una parentesi chiusa, lancia un'eccezione
        throw new IllegalArgumentException(String.format(
                "Unexpected char at %d instead of closed bracket: '%s'",
                cursor,
                string.charAt(cursor)
        ));
      }

      // Identifica il tipo di operatore
      Operator.Type operatorType = null;
      String operatorString = string.substring(operatorToken.start, operatorToken.end);
      for (Operator.Type type : Operator.Type.values()) {  // Scorre tutti i tipi di operatori
        if (operatorString.equals(Character.toString(type.getSymbol()))) {  // Se trova un match con il simbolo
          operatorType = type;
          break;
        }
      }

      if (operatorType == null) {
        // Se non riconosce l'operatore, lancia un'eccezione
        throw new IllegalArgumentException(String.format(
                "Unknown operator at %d: '%s'",
                operatorToken.start,
                operatorString
        ));
      }

      // Crea un nodo Operator con il tipo di operatore e i due figli (sottoalberi)
      return new Operator(operatorType, Arrays.asList(child1, child2));
    }

    // Se non trova né una costante, né una variabile, né una parentesi, lancia un'eccezione
    throw new IllegalArgumentException(String.format(
            "Unexpected char at %d: '%s'",
            cursor,
            string.charAt(cursor)
    ));
  }

}
