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

package org.tzi.use.parser.ocl;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.tzi.use.config.Options;
import org.tzi.use.parser.Context;
import org.tzi.use.parser.ExprContext;
import org.tzi.use.parser.SemanticException;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MNavigableElement;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.ExpAttrOp;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.uml.ocl.expr.ExpNavigation;
import org.tzi.use.uml.ocl.expr.ExpObjAsSet;
import org.tzi.use.uml.ocl.expr.ExpObjOp;
import org.tzi.use.uml.ocl.expr.ExpStdOp;
import org.tzi.use.uml.ocl.expr.ExpTupleSelectOp;
import org.tzi.use.uml.ocl.expr.ExpVariable;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.type.CollectionType;
import org.tzi.use.uml.ocl.type.ObjectType;
import org.tzi.use.uml.ocl.type.TupleType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.util.StringUtil;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author  Mark Richters
 */
public class ASTOperationExpression extends ASTExpression {
    private Token fOp;
    private ASTExpression fSrcExpr;
    private List<ASTExpression> fArgs; 
    private boolean fHasParentheses;
    private boolean fFollowsArrow;
    private Expression[] fArgExprs;
    private Token fExplicitRolename;

    public ASTOperationExpression(Token op, 
                                  ASTExpression source, 
                                  boolean followsArrow) {
        fOp = op;
        fSrcExpr = source;
        fArgs = new ArrayList<ASTExpression>();
        fHasParentheses = false;
        fFollowsArrow = followsArrow;
    }

    public void addArg(ASTExpression arg) {
        fArgs.add(arg);
    }

    public void hasParentheses() {
        fHasParentheses = true;
    }

    public void setExplicitRolename( Token rolename ) {
        fExplicitRolename = rolename;
    }

    /*
      operation may be one of: 
      (1) predefined OCL operation
      (2) attribute operation on object type (no arguments)
      (3) "isQuery" operation on object type (possibly with arguments)
      (4) navigation operation on object type
      (5) shorthand for collect
      (6) set operation on single object resulting from
      navigation over associations with multiplicity zero or one
      (p. 7-13 of OMG UML 1.3)
      (7) variable

      possible combinations of syntax:
      source expression:
      none or
      s: simple value
      o: object
      c: collection

      s.op    110 (1)
      s.op()  111 (1)
      s->op   120 (1) with warning
      s->op() 121 (1) with warning
        
      o.op    210 (2,4,1)
      o.op[nav]  1210 (4)
      o.op()  211 (3,1)
      o->op   220 (6) if s has object type and results from navigating 0..1 end
      o->op() 221 (6) to allow uniform syntax of operation calls
        
      c.op    410 (5) with implicit (2,4,1)
      c.op()  411 (5) with implicit (3,1)
      c->op   420 (1)
      c->op() 421 (1)

      op      000 (2,4,7)
      op()    001 (1,3)
    */

    public Expression gen(Context ctx) throws SemanticException {
        Expression res = null;
        Expression srcExpr = null;
        String opname = fOp.getText();

        if (fSrcExpr != null ) {
            srcExpr = fSrcExpr.gen(ctx);
            res = gen1(ctx, srcExpr);
        } else {
            // if no source expression is given, it is either a
            // variable or a reference to expression determined by
            // context. In the latter case we use the context as
            // source expression and proceed as if it has been given
            // explicitly.
            if (! fHasParentheses ) {
                // variable?
                // (7) check for variable
                Type type = ctx.varTable().lookup(opname);
                if (type != null )
                    res = new ExpVariable(opname, type);
            }

            // do we have a context expression that is implicitly
            // assumed to be the source expression?
            if (res == null ) {
                ExprContext ec = ctx.exprContext();
                if (! ec.isEmpty() ) {
                    // construct source expression
                    ExprContext.Entry e = ec.peek();
                    srcExpr = new ExpVariable(e.fName, e.fType);
                    if (e.fType.isCollection() )
                        fFollowsArrow = true;
                    res = gen1(ctx, srcExpr);
                } else
                    throw new SemanticException(fOp, "Undefined " + 
                                                ( fHasParentheses ? "operation" : "variable" ) + 
                                                " `" + opname + "'.");
            }
        }

        if (isPre() ) {
            if (! ctx.insidePostCondition() ) 
                throw new SemanticException(fOp, 
                                            "Modifier @pre is only allowed in postconditions.");
            res.setIsPre();
        }

        if (opname.equals("oclIsNew") ) {
            if (! ctx.insidePostCondition() ) 
                throw new SemanticException(fOp, 
                                            "Operation oclIsNew is only allowed in postconditions.");
        }
        return res;
    }
    

    private Expression gen1(Context ctx, Expression srcExpr) 
        throws SemanticException 
    {
        Expression res = null;
        String opname = fOp.getText();
        Type srcType = srcExpr.type();
        
        // generate argument expressions
        fArgExprs = new Expression[fArgs.size() + 1];
        fArgExprs[0] = srcExpr;
        int i = 1;

        for (ASTExpression astExpr : fArgs) {
            fArgExprs[i++] = astExpr.gen(ctx);
        }
    
        // flags for various cases
        final int SRC_SIMPLE_TYPE     = 0x0100;
        final int SRC_OBJECT_TYPE     = 0x0200;
        final int SRC_COLLECTION_TYPE = 0x0400;
        final int SRC_TUPLE_TYPE      = 0x0800;

        final int DOT                 = 0x0010;
        final int ARROW               = 0x0020;

        final int NO_EXPLICIT_ROLENAME = 0x0000;
        final int EXPLICIT_ROLENAME = 0x1000;

        final int NO_PARENTHESES      = 0x0000;
        final int PARENTHESES         = 0x0001;

        int opcase;
        if (srcType.isObjectType() )
            opcase = SRC_OBJECT_TYPE;
        else if (srcType.isCollection() )
            opcase = SRC_COLLECTION_TYPE;
        else if (srcType.isTupleType() ) 
            opcase = SRC_TUPLE_TYPE;
        else
            opcase = SRC_SIMPLE_TYPE;

        opcase += fFollowsArrow ? ARROW : DOT;
        opcase += fHasParentheses ? PARENTHESES : NO_PARENTHESES;
        opcase += fExplicitRolename != null ? EXPLICIT_ROLENAME : NO_EXPLICIT_ROLENAME;

        switch ( opcase ) {
        case SRC_SIMPLE_TYPE + DOT + NO_PARENTHESES: 
        case SRC_SIMPLE_TYPE + DOT + PARENTHESES:
        case SRC_COLLECTION_TYPE + ARROW + PARENTHESES:
        case SRC_COLLECTION_TYPE + ARROW + NO_PARENTHESES:
            // (1) predefined OCL operation
            res = genStdOperation(ctx, fOp, opname, fArgExprs);
            break;

        case SRC_SIMPLE_TYPE + ARROW + NO_PARENTHESES:
        case SRC_SIMPLE_TYPE + ARROW + PARENTHESES:
            ctx.reportWarning(fOp, "application of `" + opname + 
                              "' to a single value should be done with `.' " +
                              "instead of `->'.");
            // (1) predefined OCL operation
            res = genStdOperation(ctx, fOp, opname, fArgExprs);
            break;

        case SRC_OBJECT_TYPE + DOT + NO_PARENTHESES:
            MClass srcClass = ((ObjectType) srcType).cls();
            MAttribute attr = srcClass.attribute(opname, true);
            if (attr != null ) {
                // (2) attribute operation on object type (no arguments)
                res = new ExpAttrOp(attr, srcExpr);
            } else {
                // (4) navigation operation on object type
                // must be a role name
                MNavigableElement dst = 
                    srcClass.navigableEnd(opname);
                if (dst != null ){
                    res = genNavigation(fOp, srcClass, srcExpr, dst);
                } else {
                    // (1) predefined OCL operation
                    res = genStdOperation(ctx, fOp, opname, fArgExprs);
                }
            }
            break;

        case SRC_OBJECT_TYPE + DOT + NO_PARENTHESES + EXPLICIT_ROLENAME:
            MClass srcClass3 = ( ( ObjectType ) srcType ).cls();
            // (4) navigation operation on object type
            // must be a role name
            MNavigableElement dst =
                srcClass3.navigableEnd( opname );
            if (dst == null) {
            	//TODO: Handle error!!!
            	System.out.println("");
            } else {
            	res = genNavigation( fOp, srcClass3, srcExpr, dst, fExplicitRolename );
            }
            
            break;

        case SRC_OBJECT_TYPE + DOT + PARENTHESES:
            // (3) "isQuery" operation on object type (possibly with
            // arguments) or (1)
            MClass srcClass2 = ((ObjectType) srcType).cls();
            res = genObjOperation(ctx, srcClass2, srcExpr);
            break;

        case SRC_OBJECT_TYPE + ARROW + NO_PARENTHESES:
        case SRC_OBJECT_TYPE + ARROW + PARENTHESES:
            // (6) set operation on single object resulting from
            // navigation over associations with multiplicity zero
            // or one (p. 7-13 of OMG UML 1.3)
            if (srcExpr instanceof ExpNavigation ) {
                // first map object to set with internal operation
                Expression mappedSrcExpr = new ExpObjAsSet(srcExpr);
                // replace receiver in arg list
                fArgExprs[0] = mappedSrcExpr;
                try {
                    // lookup collection operation
                    res = ExpStdOp.create(opname, fArgExprs);
                } catch (ExpInvalidException ex) {
                    throw new SemanticException(fOp, ex);
                }
            } else {
                throw new SemanticException(fOp,
                                            "An arrow operation treating a single object as a " +
                                            "set may only be applied, if the object results " +
                                            "from a navigation to an association end with " +
                                            "multiplicity 0..1.");
            }
            break;

        case SRC_COLLECTION_TYPE + DOT + NO_PARENTHESES:
            // c.op    200 (5) with implicit (2,4,1)
            if (Options.disableCollectShorthand )
                throw new SemanticException(fOp, MSG_DISABLE_COLLECT_SHORTHAND);
            res = collectShorthandWithOutArgs(opname, srcExpr);
            break;

        case SRC_COLLECTION_TYPE + DOT + PARENTHESES:
            // c.op()  201 (5) with implicit (3,1)
            if (Options.disableCollectShorthand )
                throw new SemanticException(fOp, MSG_DISABLE_COLLECT_SHORTHAND);
            res = collectShorthandWithArgs(opname, srcExpr);
        break;
        case SRC_TUPLE_TYPE + DOT + PARENTHESES:
        case SRC_TUPLE_TYPE + DOT + NO_PARENTHESES:
            TupleType t = (TupleType)srcType;
            TupleType.Part p = t.getPart(opname);
            if (p==null) {
                // try std operation
                res = genStdOperation(ctx, fOp, opname, fArgExprs);
            } else {
                res = new ExpTupleSelectOp(p, srcExpr);
            }
        break;
        case SRC_TUPLE_TYPE + ARROW + PARENTHESES:
        case SRC_TUPLE_TYPE + ARROW + NO_PARENTHESES:
	    throw new SemanticException(fOp, "Collection operation not applicable to tuple type.");

        //          throw new SemanticException(fOp,
        /*          "If you want to apply an operation to a
         *          collection, please use an `->' instead of a
         *          `.'. If you are trying to apply the shorthand
         *          notation for `collect' - sorry, this is not yet
         *          supported. Please use the explicit notation.");
         */
        default:
            throw new RuntimeException("case " + Integer.toHexString(opcase) + 
                                       " not handled");
        }
        
        if (isPre() )
            res.setIsPre();
        return res;
    }

    /**
     * Handles shorthand notation for collect and expands to an
     * explicit collect expression. 
     */
    private Expression collectShorthandWithOutArgs(String opname, Expression srcExpr)
        throws SemanticException 
    {
        Expression res = null;
        // c.op    200 (5) with implicit (2,4,1)
        CollectionType cType = (CollectionType ) srcExpr.type();
        Type elemType = cType.elemType();
        if (elemType.isObjectType() ) {
            MClass srcClass = ((ObjectType) elemType).cls();
            MAttribute attr = srcClass.attribute(opname, true);
            if (attr != null ) {
                // (2) attribute operation on object type (no arguments)

                // transform c.a into c->collect($e | $e.a)
                ExpVariable eVar = new ExpVariable("$e", elemType);
                ExpAttrOp eAttr = new ExpAttrOp(attr, eVar);
                res = genImplicitCollect(srcExpr, eAttr, elemType);
            } else {
                MNavigableElement dst = srcClass.navigableEnd(opname);
                if (dst != null ) {
                    // (4) navigation operation on object type must be
                    // a role name

                    // transform c.r into c->collect($e | $e.r)
                    ExpVariable eVar = new ExpVariable("$e", elemType);
                    Expression eNav = genNavigation(fOp, srcClass, eVar, dst);
                    res = genImplicitCollect(srcExpr, eNav, elemType);
                }
            }
        }
        if (res == null ) {
            // ! objectType || ! (attr ||  navigation)
            // try (1) predefined OCL operation
            res = collectShorthandStdOp(opname, srcExpr, cType, elemType);
        }
        return res;
    }


    /**
     * Handles shorthand notation for collect and expands to an
     * explicit collect expression. 
     */
    private Expression collectShorthandWithArgs(String opname, Expression srcExpr) 
        throws SemanticException
    {
        Expression res = null;
        // c.op()  201 (5) with implicit (3,1)
        CollectionType cType = (CollectionType ) srcExpr.type();
        Type elemType = cType.elemType();
        if (elemType.isObjectType() ) {
            MClass srcClass = ((ObjectType) elemType).cls();
            MOperation op = srcClass.operation(opname, true);
            if (op != null ) {
                // check for isQuery operation with OCL body
                if (!op.hasExpression())
                    throw new SemanticException
                        (fOp, "Operation `" + srcClass.name() + "::" + 
                         op.signature() + "' cannot be used in OCL expression " +
                         "(only side effect-free operations with a return type" +
                         " and an OCL expression as body may be used).");
                
                // transform c.op(...) into c->collect($e | $e.op(...))
                ExpVariable eVar = new ExpVariable("$e", elemType);
                fArgExprs[0] = eVar;
                try { 
                    // constructor performs additional checks
                    res = new ExpObjOp(op, fArgExprs);
                } catch (ExpInvalidException ex) {
                    throw new SemanticException(fOp, 
                                                "In operation call `" + srcClass.name() + "::" + 
                                                opname + "': " + ex.getMessage());
                }
                res = genImplicitCollect(srcExpr, res, elemType);
            }
        }
        if (res == null ) {
            // try (1) predefined OCL operation
            res = collectShorthandStdOp(opname, srcExpr, cType, elemType);
        }
        return res;
    }
    

    /**
     * Handles shorthand notation for collect and expands to an
     * explicit collect expression. 
     */
    private Expression collectShorthandStdOp(String opname, 
                                             Expression srcExpr, 
                                             Type cType,
                                             Type elemType)
        throws SemanticException 
    {
        Expression res = null;
        // (1) predefined OCL operation

        // find operation on element type
        Type[] params = new Type[fArgExprs.length];
        params[0] = elemType;
        for (int i = 1; i < fArgExprs.length; i++)
            params[i] = fArgExprs[i].type();
        if (! ExpStdOp.exists(opname, params) ) {
            String msg = "Undefined operation `" + elemType +
                "." + opname + "' in shorthand notation for collect.";
            // maybe the user just confused . with ->, check for
            // operation on collection and give a hint.
            params[0] = cType;
            if (ExpStdOp.exists(opname, params) )
                msg += " However, there is an operation `" + cType + 
                    "->" + opname + 
                    "'. Maybe you wanted to use `->' instead of `.'?";
            throw new SemanticException(fOp, msg);
        }
        // transform c.op(...) into c->collect($e | $e.op(...))
        fArgExprs[0] = new ExpVariable("$e", elemType);
        try {
            Expression eOp = ExpStdOp.create(opname, fArgExprs);
            res = genImplicitCollect(srcExpr, eOp, elemType);
        } catch (ExpInvalidException ex) {
            // shouldn't fail because we already checked the
            // existence above
            throw new RuntimeException("collectShorthand failed: " + ex.getMessage());
        }
        return res;
    }


    // checks (3) and (1)
    private Expression genObjOperation(Context ctx, 
                                       MClass srcClass, Expression srcExpr)
        throws SemanticException
    {
        Expression res = null;

        // find operation
        String opname = fOp.getText();
        MOperation op = srcClass.operation(opname, true);
        if (op != null ) {
            
            // check for isQuery operation with OCL body
            if (ctx.isSideEffectFree() && !(op.hasExpression() || op.hasScript()))
                throw new SemanticException
                    (fOp,"Operation `" + srcClass.name() + "::" + 
                     op.signature() + "' cannot be used in OCL expression " +
                     "(only side effect-free operations with a return type" + 
                     " and an OCL expression as body may be used).");
            
            try { 
                // constructor performs additional checks
                res = new ExpObjOp(op, fArgExprs);
            } catch (ExpInvalidException ex) {
                throw new SemanticException(fOp, 
                                            "In operation call `" + srcClass.name() + "::" + 
                                            opname + "': " + ex.getMessage());
            }
        } else {
            // try standard operation
            res = genStdOperation(ctx, fOp, opname, fArgExprs);
        }
        return res;
    }

    public String toString() {
        return "(" + fOp + " " + StringUtil.fmtSeq(fArgs.iterator(), " ") + ")";
    }
}
