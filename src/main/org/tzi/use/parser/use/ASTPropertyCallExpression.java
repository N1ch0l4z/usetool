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

/* $ProjectHeader: use 0.393 Wed, 16 May 2007 14:10:28 +0200 opti $ */

package org.tzi.use.parser.use;

import java.util.ArrayList;
import java.util.List;

import org.tzi.use.parser.Context;
import org.tzi.use.parser.MyToken;
import org.tzi.use.parser.SemanticException;
import org.tzi.use.parser.ocl.ASTExpression;
import org.tzi.use.uml.ocl.expr.Expression;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author  Mark Richters
 */
public class ASTPropertyCallExpression extends ASTExpression {
    private MyToken fOp;
    private List fArgs;     // (ASTExpression) may be empty!

    public ASTPropertyCallExpression(MyToken token, ASTExpression source) {
        fOp = token;
        fArgs = new ArrayList();
        if (source != null )
            fArgs.add(source);
    }

    public void addArg(ASTExpression arg) {
        fArgs.add(arg);
    }

    public Expression gen(Context ctx) throws SemanticException {
        Expression res = null;

        return res;
    }

    public String toString() {
        return super.toString() + fOp.toString();
    }
}