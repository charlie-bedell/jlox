package com.jlox.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jlox.lox.TokenType.*;

class Scanner {
		private final String source;
		private final List<Token> tokens = new ArrayList<>();
		private int start = 0;
		private int current = 0;
		private int line = 1;

		private static final Map<String, TokenType> keywords;

		static {
				keywords = new HashMap<>();
				keywords.put("and", AND);
				keywords.put("class", CLASS);
				keywords.put("else", ELSE);
				keywords.put("false", FALSE);
				keywords.put("for", FOR);
				keywords.put("fun", FUN);
				keywords.put("if", IF);
				keywords.put("nil", NIL);
				keywords.put("or", OR);
				keywords.put("print", PRINT);
				keywords.put("return", RETURN);
				keywords.put("super", SUPER);
				keywords.put("this", THIS);
				keywords.put("true", TRUE);
				keywords.put("var", VAR);
				keywords.put("while", WHILE);
		}

		private boolean isAtEnd() {
				return current >= source.length();
		}

		Scanner(String source) {
				this.source = source;
		}


		List<Token> scanTokens() {
				while (!isAtEnd()) {
						// we are at the beginning of the next lexeme
						start = current;
						scanToken();
				}

				tokens.add(new Token(EOF, "", null, line));
				return tokens;
		}

		private void scanToken() {
				char c = advance();

				switch (c) {
				case '(': addToken(LEFT_PAREN); break;
				case ')': addToken(RIGHT_PAREN); break;
				case '{': addToken(LEFT_BRACE); break;
				case '}': addToken(RIGHT_BRACE); break;
				case ',': addToken(COMMA); break;
				case '.': addToken(DOT); break;
				case '-': addToken(MINUS); break;
				case '+': addToken(PLUS); break;
				case ';': addToken(SEMICOLON); break;
				case '*': addToken(STAR); break;
				case '!':
						addToken(match('=') ? BANG_EQUAL : BANG);
				case '=':
						addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				case '<':
						addToken(match('=') ? LESS_EQUAL : LESS);
				case '>':
						addToken(match('=') ? GREATER_EQUAL : EQUAL);
				case '/':
						if (match('/')) {
								// A comment goes until the end of the line.
								while (peek() != '\n' && !isAtEnd()) advance();
						} else if (match('*')) {
								multiLineComment();
						} else {
								addToken(SLASH);
						}
						break;

				case ' ':
				case '\t':
				case '\r':
						break;
				case '\n':
						line++;
						break;

				case '"': string(); break;

				default:
						if (isDigit(c)) {
								number();
						} else if (isAlpha(c)) {
								identifier();
						} else {
								Lox.error(line, "Unexpected character.");
						}
						break;
				}
		}

		private void identifier() {
				while (isAlphaNumeric(peek())) advance();

				String text = source.substring(start, current);
				TokenType type = keywords.get(text);
				addToken(type);
				
		}

		private void number() {
				while (isDigit(peek())) advance();

				if (peek() == '.' && isDigit(peekNext())) {
						// Consume the "."
						advance();

						while (isDigit(peek())) advance();
				}

				addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
		}

		private void string() {
				while (peek() != '"' && !isAtEnd()) {
						if (peek() == '\n') line++;
						advance();
				}

				if (isAtEnd()) {
						Lox.error(line, "unterminated string");
						return;
				}

				advance(); // The closing ".

				// Trim surrounding quotes.
				String value = source.substring(start + 1, current - 1);
				addToken(STRING, value);
		}

		private void multiLineComment() {
				int multiLineCommentDepth = 1;
				while (multiLineCommentDepth > 0 && !isAtEnd()) {
						if (peek() == '\n') {
								line++;
						} else if ((peek() == '/') && (peekNext() == '*')) {
								multiLineCommentDepth++;
						} else if ((peek() == '*') && (peekNext() == '/')) {
								multiLineCommentDepth--;
						}
						if (isAtEnd()) {
								Lox.error(line, "unterminated comment");
								return;
						}
						advance();
				}
				advance();
		}

		private boolean match(char expected) {
				if (isAtEnd()) return false;
				if (source.charAt(current) != expected) return false;

				current++;
				return true;
		}

		private char advance() {
				return source.charAt(current++);
		}

		private char peek() {
				if (isAtEnd()) return '\0';
				return source.charAt(current);
		}

		private char peekNext() {
				if (current + 1 >= source.length()) return '\0';
				return source.charAt(current + 1);
		}

		private boolean isAlpha(char c) {
				return (c >= 'a' && c <= 'z') ||
						   (c >= 'A' && c <= 'Z') ||
						    c == '_';
		}

		private boolean isAlphaNumeric(char c) {
				return isAlpha(c) || isDigit(c);
		}

		private boolean isDigit(char c) {
				return c >= '0' && c <= '9';
		}

		private void addToken(TokenType type) {
				addToken(type, null);
		}

		// java function overload, use either depending on signature
		private void addToken(TokenType type, Object literal) {
				String text = source.substring(start, current);
				tokens.add(new Token(type, text, literal, line));
		}

		
		
}
