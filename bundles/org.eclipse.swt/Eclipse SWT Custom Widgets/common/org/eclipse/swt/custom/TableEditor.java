/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;


import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
*
* A TableEditor is a manager for a Control that appears above a cell in a Table and tracks with the
* moving and resizing of that cell.  It can be used to display a text widget above a cell
* in a Table so that the user can edit the contents of that cell.  It can also be used to display
* a button that can launch a dialog for modifying the contents of the associated cell.
*
* <p> Here is an example of using a TableEditor:
* <code><pre>
*	final Table table = new Table(shell, SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
*	TableColumn column1 = new TableColumn(table, SWT.NONE);
*	TableColumn column2 = new TableColumn(table, SWT.NONE);
*	for (int i = 0; i &lt 10; i++) {
*		TableItem item = new TableItem(table, SWT.NONE);
*		item.setText(new String[] {"item " + i, "edit this value"});
*	}
*	column1.pack();
*	column2.pack();
*	
*	final TableEditor editor = new TableEditor(table);
*	//The editor must have the same size as the cell and must
*	//not be any smaller than 50 pixels.
*	editor.horizontalAlignment = SWT.LEFT;
*	editor.grabHorizontal = true;
*	editor.minimumWidth = 50;
*	// editing the second column
*	final int EDITABLECOLUMN = 1;
*	
*	table.addSelectionListener(new SelectionAdapter() {
*		public void widgetSelected(SelectionEvent e) {
*			// Clean up any previous editor control
*			Control oldEditor = editor.getEditor();
*			if (oldEditor != null) oldEditor.dispose();
*	
*			// Identify the selected row
*			TableItem item = (TableItem)e.item;
*			if (item == null) return;
*	
*			// The control that will be the editor must be a child of the Table
*			Text newEditor = new Text(table, SWT.NONE);
*			newEditor.setText(item.getText(EDITABLECOLUMN));
*			newEditor.addModifyListener(new ModifyListener() {
*				public void modifyText(ModifyEvent e) {
*					Text text = (Text)editor.getEditor();
*					editor.getItem().setText(EDITABLECOLUMN, text.getText());
*				}
*			});
*			newEditor.selectAll();
*			newEditor.setFocus();
*			editor.setEditor(newEditor, item, EDITABLECOLUMN);
*		}
*	});
* </pre></code>
*/
public class TableEditor extends ControlEditor {
	Table table;
	TableItem item;
	int column = -1;
	ControlListener columnListener;
/**
* Creates a TableEditor for the specified Table.
*
* @param table the Table Control above which this editor will be displayed
*
*/
public TableEditor (Table table) {
	super(table);
	this.table = table;
	
	columnListener = new ControlListener() {
		public void controlMoved(ControlEvent e){
			resize ();
		}
		public void controlResized(ControlEvent e){
			resize ();
		}
	};
	
	// To be consistent with older versions of SWT, grabVertical defaults to true
	grabVertical = true;
}
Rectangle computeBounds () {
	if (item == null || column == -1 || item.isDisposed()) return new Rectangle(0, 0, 0, 0);
	Rectangle cell = item.getBounds(column);
	Rectangle area = table.getClientArea();
	if (cell.x < area.x + area.width) {
		if (cell.x + cell.width > area.x + area.width) {
			cell.width = area.x + area.width - cell.x;
		}
	}
	Rectangle editorRect = new Rectangle(cell.x, cell.y, minimumWidth, minimumHeight);

	if (grabHorizontal) {
		editorRect.width = Math.max(cell.width, minimumWidth);
	}
	
	if (grabVertical) {
		editorRect.height = Math.max(cell.height, minimumHeight);
	}
	
	if (horizontalAlignment == SWT.RIGHT) {
		editorRect.x += cell.width - editorRect.width;
	} else if (horizontalAlignment == SWT.LEFT) {
		// do nothing - cell.x is the right answer
	} else { // default is CENTER
		editorRect.x += (cell.width - editorRect.width)/2;
	}
	
	if (verticalAlignment == SWT.BOTTOM) {
		editorRect.y += cell.height - editorRect.height;
	} else if (verticalAlignment == SWT.TOP) {
		// do nothing - cell.y is the right answer
	} else { // default is CENTER
		editorRect.y += (cell.height - editorRect.height)/2;
	}
	return editorRect;
}
/**
 * Removes all associations between the TableEditor and the cell in the table.  The
 * Table and the editor Control are <b>not</b> disposed.
 */
public void dispose () {
	if (this.column > -1 && this.column < table.getColumnCount()){
		TableColumn tableColumn = table.getColumn(this.column);
		tableColumn.removeControlListener(columnListener);
	}
	columnListener = null;
	table = null;
	item = null;
	column = -1;
	
	super.dispose();
}
/**
* Returns the zero based index of the column of the cell being tracked by this editor.
*
* @return the zero based index of the column of the cell being tracked by this editor
*/
public int getColumn () {
	return column;
}
/**
* Returns the TableItem for the row of the cell being tracked by this editor.
*
* @return the TableItem for the row of the cell being tracked by this editor
*/
public TableItem getItem () {
	return item;
}
public void setColumn(int column) {
	int columnCount = table.getColumnCount();
	// Separately handle the case where the table has no TableColumns.
	// In this situation, there is a single default column.
	if (columnCount == 0) {
		this.column = (column == 0) ? 0 : -1;
		resize();
		return;
	}
	if (this.column > -1 && this.column < columnCount){
		TableColumn tableColumn = table.getColumn(this.column);
		tableColumn.removeControlListener(columnListener);
		this.column = -1;
	}

	if (column < 0  || column >= table.getColumnCount()) return;	
		
	this.column = column;
	TableColumn tableColumn = table.getColumn(this.column);
	tableColumn.addControlListener(columnListener);
	resize();
}
public void setItem (TableItem item) {	
	this.item = item;
	resize();
}

/**
* Specify the Control that is to be displayed and the cell in the table that it is to be positioned above.
*
* <p>Note: The Control provided as the editor <b>must</b> be created with its parent being the Table control
* specified in the TableEditor constructor.
* 
* @param editor the Control that is displayed above the cell being edited
* @param item the TableItem for the row of the cell being tracked by this editor
* @param column the zero based index of the column of the cell being tracked by this editor
*/
public void setEditor (Control editor, TableItem item, int column) {
	setItem(item);
	setColumn(column);
	setEditor(editor);
}
void resize () {
	if (table.isDisposed()) return;
	if (item == null || item.isDisposed()) return;
	int columnCount = table.getColumnCount();
	if (columnCount == 0 && column != 0) return;
	if (columnCount > 0 && (column < 0 || column >= columnCount)) return;
	super.resize();
}
}
