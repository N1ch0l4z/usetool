// $ANTLR 2.7.4: "expandeduse.g" -> "GUSEParser.java"$
 
/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  
 */

package org.tzi.use.parser.use; 

public interface GUSELexerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LPAREN = 4;
	int COMMA = 5;
	int RPAREN = 6;
	int IDENT = 7;
	int COLON = 8;
	int LITERAL_let = 9;
	int EQUAL = 10;
	int LITERAL_in = 11;
	int LITERAL_implies = 12;
	int LITERAL_or = 13;
	int LITERAL_xor = 14;
	int LITERAL_and = 15;
	int NOT_EQUAL = 16;
	int LESS = 17;
	int GREATER = 18;
	int LESS_EQUAL = 19;
	int GREATER_EQUAL = 20;
	int PLUS = 21;
	int MINUS = 22;
	int STAR = 23;
	int SLASH = 24;
	int LITERAL_div = 25;
	int LITERAL_not = 26;
	int ARROW = 27;
	int DOT = 28;
	int LITERAL_allInstances = 29;
	int AT = 30;
	int LITERAL_pre = 31;
	int BAR = 32;
	int LITERAL_iterate = 33;
	int SEMI = 34;
	int LBRACK = 35;
	int RBRACK = 36;
	int LITERAL_oclAsType = 37;
	int LITERAL_oclIsKindOf = 38;
	int LITERAL_oclIsTypeOf = 39;
	int LITERAL_if = 40;
	int LITERAL_then = 41;
	int LITERAL_else = 42;
	int LITERAL_endif = 43;
	int LITERAL_true = 44;
	int LITERAL_false = 45;
	int INT = 46;
	int REAL = 47;
	int STRING = 48;
	int HASH = 49;
	int LITERAL_Set = 50;
	int LITERAL_Sequence = 51;
	int LITERAL_Bag = 52;
	int LBRACE = 53;
	int RBRACE = 54;
	int DOTDOT = 55;
	int LITERAL_oclEmpty = 56;
	int LITERAL_oclUndefined = 57;
	int LITERAL_Tuple = 58;
	int LITERAL_Collection = 59;
	int LITERAL_model = 60;
	int LITERAL_constraints = 61;
	int LITERAL_enum = 62;
	int LITERAL_abstract = 63;
	int LITERAL_class = 64;
	int LITERAL_attributes = 65;
	int LITERAL_operations = 66;
	int LITERAL_end = 67;
	int LITERAL_associationClass = 68;
	int LITERAL_associationclass = 69;
	int LITERAL_between = 70;
	int LITERAL_aggregation = 71;
	int LITERAL_composition = 72;
	int LITERAL_begin = 73;
	int LITERAL_association = 74;
	int LITERAL_role = 75;
	int LITERAL_ordered = 76;
	int LITERAL_context = 77;
	int LITERAL_inv = 78;
	int COLON_COLON = 79;
	int LITERAL_post = 80;
	int LITERAL_var = 81;
	int LITERAL_declare = 82;
	int LITERAL_set = 83;
	int COLON_EQUAL = 84;
	int LITERAL_create = 85;
	int LITERAL_namehint = 86;
	int LITERAL_insert = 87;
	int LITERAL_into = 88;
	int LITERAL_delete = 89;
	int LITERAL_from = 90;
	int LITERAL_destroy = 91;
	int LITERAL_while = 92;
	int LITERAL_do = 93;
	int LITERAL_wend = 94;
	int LITERAL_for = 95;
	int LITERAL_execute = 96;
	int WS = 97;
	int SL_COMMENT = 98;
	int ML_COMMENT = 99;
	int RANGE_OR_INT = 100;
	int ESC = 101;
	int HEX_DIGIT = 102;
	int VOCAB = 103;
}