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

// $Id$

package org.tzi.use.parser.cmd;

import org.antlr.runtime.Token;
import org.tzi.use.parser.Context;
import org.tzi.use.parser.SemanticException;
import org.tzi.use.parser.SrcPos;
import org.tzi.use.parser.ocl.ASTExpression;
import org.tzi.use.uml.ocl.expr.ExpAttrOp;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.sys.MCmd;
import org.tzi.use.uml.sys.MCmdSetAttribute;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author  Mark Richters
 */
public class ASTSetCmd extends ASTCmd {
    private ASTExpression fAttrExpr;
    private ASTExpression fSetExpr;

    public ASTSetCmd(Token start, ASTExpression attrExpr, ASTExpression setExpr) {
        super(start);
    	fAttrExpr = attrExpr;
        fSetExpr = setExpr;
    }

    public MCmd gen(Context ctx) 
        throws SemanticException
    {
        Expression e = fAttrExpr.gen(ctx);
        if (! (e instanceof ExpAttrOp) )
            throw new SemanticException(fAttrExpr.getStartToken(), 
                                        "Expression does not give a reference to an attribute.");
        ExpAttrOp attrExpr = (ExpAttrOp) e;
        Expression setExpr = fSetExpr.gen(ctx);

        // check type conformance of assignment
        if (! setExpr.type().isSubtypeOf(attrExpr.type()) )
            throw new SemanticException(fSetExpr.getStartToken(), 
                                        "Type mismatch in assignment expression. " +
                                        "Expected type `" + attrExpr.type() + "', found `" +
                                        setExpr.type() + "'.");    

        return new MCmdSetAttribute(getPosition(), ctx.systemState(),
                                    attrExpr, 
                                    setExpr);
    }

    public SrcPos getPosition() {
    	return new SrcPos(fAttrExpr.getStartToken());
    }
    
	@Override
	public String toString() {
		return "set";
	}
}
