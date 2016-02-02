package org.eclipse.ui.plugin.handlers;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.plugin.utils.EclipseConstants;

public class GenerateToStringHandler extends AbstractHandler {
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// this object is needed to render wizards, messages and so on
		Shell activeShell = HandlerUtil.getActiveShell(event);
		// get selected items or text
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		// identify active GUI part
		String activePartId = HandlerUtil.getActivePartId(event);
		// java editor must be handled differently than view selection
		if (EclipseConstants.JAVA_EDITOR_ID.equals(activePartId)) {
			// get edited file
			IEditorInput input = HandlerUtil.getActiveEditorInput(event);


			IJavaElement elem = JavaUI.getEditorInputJavaElement(input);
			if (elem.exists()) {

				ICompilationUnit unit = (ICompilationUnit) elem;
				// parse compilation unit
				
				CompilationUnit astRoot = createAST(unit);

				List<IField> fieldlist = new ArrayList<IField>();
				try {
					for (IField field : unit.getTypes()[0].getFields()) {
						fieldlist.add(field);
					}
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					

				// create a ASTRewrite
				AST ast = astRoot.getAST();
				ASTRewrite rewriter = ASTRewrite.create(ast);

				astRoot.recordModifications();


				Block block =ast.newBlock();	
				block = createToStringMethodStatements(ast,fieldlist);
				
				MethodDeclaration methodDeclaration2 = createToStringMethod(ast, Modifier.PUBLIC, false, block);


				ListRewrite lrw2 = rewriter.getListRewrite(((TypeDeclaration) astRoot.types().get(0)), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				lrw2.insertLast(methodDeclaration2, null);
				



				try {
					TextEdit edits = rewriter.rewriteAST();
					Document document = new Document(unit.getSource());
					edits.apply(document);
					// this is the code for adding statements
					unit.getBuffer().setContents(document.get());
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (org.eclipse.jface.text.BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String a = "asdasdsad";
			a.getBytes();

			// currentSelection now contains text selection inside input file
			// ... locate class selected in that file ...
		} else {
			// currentSelection now contains all classes inside
			// ... collect all selected classes ...
		}

		System.out.println("GenerateToStringHandler");
		return null;
	}

	
	private MethodDeclaration createToStringMethod(AST ast, int modifiers, boolean isConstructor ,Block block) {
		MethodDeclaration declaration = ast.newMethodDeclaration();
		declaration.setName(ast.newSimpleName("toString"));
		declaration.setReturnType2(ast.newSimpleType(ast.newSimpleName("String")));
		declaration.modifiers().addAll(ast.newModifiers(modifiers));
		declaration.setConstructor(isConstructor);
		declaration.setBody(block);//bu boş zaten !!
		
		//burası da boş
//		SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
//		newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
//		newParam.setName(ast.newSimpleName("p1"));
//		declaration.parameters().add(newParam);
		return declaration;
	}


	private CompilationUnit createAST(ICompilationUnit compilationUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}


	private Block createToStringMethodStatements(AST ast, List<IField> fieldList) {
	    Block result = ast.newBlock();
	    ReturnStatement returnStatement = createToStringReturnStatement(ast, fieldList);
	    result.statements().add(returnStatement);
	    return result;
	}
	
	

	private VariableDeclarationStatement createVariableDeclarationStatement(AST ast) {
		
	    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
	    fragment.setName(ast.newSimpleName("con"));
	    fragment.setInitializer(ast.newNullLiteral());//
	    VariableDeclarationStatement result = ast.newVariableDeclarationStatement(fragment);
	    TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
	    typeDeclaration.setName(ast.newSimpleName("Connection"));
	    result.setType(ast.newSimpleType(ast.newSimpleName("Connection")));
	    return result;
	}

	private ReturnStatement createToStringReturnStatement(AST ast, List<IField> fieldList) {
		ReturnStatement result = ast.newReturnStatement();
		
		
		InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		

		int counter=0;
		for (IField field : fieldList) {
			InfixExpression infixExpressionInner = ast.newInfixExpression();
			infixExpressionInner.setOperator(InfixExpression.Operator.PLUS);
			
			StringLiteral literal = ast.newStringLiteral();
			if (counter!=0) {
				literal.setLiteralValue(", " + field.getElementName() + ": ");
			}else{
				literal.setLiteralValue(" " + field.getElementName() + ": ");
			}

			
			infixExpressionInner.setLeftOperand(literal);
			infixExpressionInner.setRightOperand(ast.newSimpleName(field.getElementName()));

			if (counter==0) {
				infixExpression.setLeftOperand(infixExpressionInner);
			} else if(counter==1){
				infixExpression.setRightOperand(infixExpressionInner);
			} else {
				infixExpression = concatInfixExpression(ast,infixExpression,infixExpressionInner);
			}
			counter++;
		}
		
		//result.setExpression(ast.newStringLiteral(" rollNo: + rollNo + " name: " + name + " city: " + city ")); //ast.newSimpleName("con")	
		if (fieldList.size()==1) {
			result.setExpression(infixExpression.getLeftOperand());
		} else if(fieldList.size()>1){
			result.setExpression(infixExpression);
		}	
		
	    return result;
	}
	
	private InfixExpression concatInfixExpression(AST ast, InfixExpression infixExpression1, InfixExpression infixExpression2){
		InfixExpression infixExpression= ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		infixExpression.setLeftOperand(infixExpression1);
		infixExpression.setRightOperand(infixExpression2);
		return infixExpression;
	}


}
