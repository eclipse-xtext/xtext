/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu
import org.eclipse.xtext.xbase.lib.Pair

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
class SwtBotProjectHelper {
	
	static val defaultProject = 'test' 

	static def newXtendProject(SWTWorkbenchBot it) {
		newXtendProject(defaultProject)
	}
	
	static def newXtendProject(SWTWorkbenchBot it, String projectName) {
		val shell = activeShell
		viewByTitle("Welcome").close
		perspectiveByLabel('Java').activate
		menu('File').menu('New').menu('Project...').click
		shell('New Project').activate
		tree.expandNode('Java').select('Java Project')
		button('Next >').click
		textWithLabel('Project name:').text = 'test'
		button('Next >').click
		tabItem('Libraries').activate
		button('Add Library...').click
		shell('Add Library').activate
		list.select('Xtend Library')
		button('Next >').click
		button('Finish').click
		button('Finish').click
		shell.activate
	}
	
	static def newJavaEditor(SWTWorkbenchBot it, String typeName) {
		newJavaEditor(typeName, '', defaultProject + '/src')
	}
	
	static def newJavaEditor(SWTWorkbenchBot it, String typeName, String packageName, String sourceFolderPath) {
		menu('File').menu('New').menu('Class').click
		shell('New Java Class').activate
		textWithLabel('Source folder:').text = sourceFolderPath
		textWithLabel('Package:').text = packageName
		textWithLabel('Name:').text = typeName
		button('Finish').click
		editorByTitle(typeName + '.java').toTextEditor
	}
	
	static def newXtendEditor(SWTWorkbenchBot it, String typeName) {
		newXtendEditor(typeName, '', defaultProject + '/src')
	}
	
	static def newXtendEditor(SWTWorkbenchBot it, String typeName, String packageName, String sourceFolderPath) {
		menu('File').menu('New').menu('Xtend Class').click
		shell('Xtend Class').activate
		textWithLabel('Source folder:').text = sourceFolderPath
		textWithLabel('Package:').text = packageName
		textWithLabel('Name:').text = typeName
		button('Finish').click
		editorByTitle(typeName + '.xtend').toTextEditor
	}
	
	static def closeAllEditors(SWTWorkbenchBot it) {
		editors.forEach[close]
	}
	
	static def setContent(SWTBotEclipseEditor it, CharSequence content) {
		val contentString = content.toString
		text = contentString.replace('?', '')
		val offset = contentString.indexOf('?')
		if(offset != -1) {
			val lineAndColumn = contentString.getLineAndColumn(offset)
			selectRange(lineAndColumn.key, lineAndColumn.value, contentString.lastIndexOf('?') - offset - 1)
		}
	}
	
	static def clickableContextMenu(SWTBotEclipseEditor editor, String... text) {
		new SWTBotMenu(ContextMenuHelper.contextMenu(editor.styledText, text))
	}
	
	protected static def Pair<Integer, Integer> getLineAndColumn(String content, int offset) {
		var line = 0
		var column = 0
		var String ignoreNext = null   
		for(i: 0..<offset) {
			switch content.substring(i, i+1) {
				case ignoreNext: 
					ignoreNext = null
				case '\n': {
					column = 0
					line = line + 1  		
					ignoreNext = '\r'
				}
				case '\r': {
					column = 0
					line = line + 1  		
					ignoreNext = '\n'
				}
				default: {
					ignoreNext = null
					column = column + 1
				}
			}
		}
		line -> column
	}
	
	static def clearSourceFolderContents(SWTWorkbenchBot it) {
		clearSourceFolderContents(defaultProject)
	}
	
	static def clearSourceFolderContents(SWTWorkbenchBot it, String project) {
		val srcNode = tree.expandNode(project).expandNode('src')
		for(source: srcNode.items) {
			if(!source.widget.disposed) {
				println(text)
				srcNode.select(source.text)
				source.contextMenu('Delete').click
				shell('Delete').activate
				button('OK').click
			}
		}
	}
}

