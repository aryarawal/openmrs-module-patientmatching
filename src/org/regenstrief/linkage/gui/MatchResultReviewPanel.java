package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

public class MatchResultReviewPanel extends JPanel implements ActionListener, ChangeListener, KeyListener{
	public static final int CERTAINTY_LEVELS = 5;
	
	private static final String MATCH_STRING = "match";
	private static final String NONMATCH_STRING = "nonmatch";
	private static final String NOT_REVIEWED_STRING = "not reviewed";
	
	JPanel score_panel, dem_panel, status_panel;
	JTextField row;
	JTextField score;
	JTextField note;
	JRadioButton not_reviewed, match, not_match;
	ButtonGroup review_status;
	JSlider certainty;
	JTextField certainty_val;
	JTable values;
	
	String[] demographics;
	MatchResult mr;
	
	public MatchResultReviewPanel(String[] demographics){
		this.demographics = demographics;
		initGUI();
	}
	
	private void initGUI(){
		this.addContainerListener(MatchResultReviewKeyboardAccelerator.INSTANCE);
		
		this.setLayout(new BorderLayout());
		
		// init and add elements of score panel
		score_panel = new JPanel();
		JLabel row_label = new JLabel("Row:");
		row = new JTextField(5);
		row.setEditable(false);
		row.setEnabled(false);
		score = new JTextField(12);
		score.setEditable(false);
		score_panel.add(row_label);
		score_panel.add(row);
		//score_panel.add(score);
		
		// init and add elements for demographic values table
		dem_panel = new JPanel();
		dem_panel.setLayout(new BorderLayout());
		Object[][] data = new Object[2][demographics.length];
		for(int i = 0; i < demographics.length; i++){
			data[0][i] = "";
			data[1][i] = "";
		}
		values = new JTable();
		values.setEnabled(false);
		DemographicReviewTableModel drtm = new DemographicReviewTableModel(data, demographics);
		values.setModel(drtm);
		
		
		JPanel dem_top = new JPanel();
		dem_top.setLayout(new BorderLayout());
		JLabel jl = new JLabel("Note:");
		JPanel dem_bottom = new JPanel();
		dem_bottom.add(jl);
		add(Box.createRigidArea(new Dimension(5,0)));
		note = new JTextField(40);
		note.addKeyListener(this);
		dem_bottom.add(note);
		
		dem_top.add(values.getTableHeader(), BorderLayout.PAGE_START);
		dem_top.add(values, BorderLayout.CENTER);
		
		dem_panel.add(dem_top, BorderLayout.PAGE_START);
		dem_panel.add(dem_bottom, BorderLayout.PAGE_END);
		
		
		// init and add elements for match status panel
		status_panel = new JPanel();
		status_panel.addKeyListener(this);
		status_panel.setLayout(new BoxLayout(status_panel, BoxLayout.PAGE_AXIS));
		not_reviewed = new JRadioButton("Not-Reviewed");
		not_reviewed.addActionListener(this);
		not_reviewed.setMnemonic(KeyEvent.VK_R);
		//not_reviewed.setActionCommand(NOT_REVIEWED_STRING);
		match = new JRadioButton("Match");
		match.addActionListener(this);
		match.setMnemonic(KeyEvent.VK_M);
		//match.setActionCommand(MATCH_STRING);
		not_match = new JRadioButton("Not-Match");
		not_match.addActionListener(this);
		not_match.setMnemonic(KeyEvent.VK_N);
		//not_match.setActionCommand(NONMATCH_STRING);
		review_status = new ButtonGroup();
		review_status.add(not_reviewed);
		review_status.add(match);
		review_status.add(not_match);
		String[] levels = new String[CERTAINTY_LEVELS];
		for(int i = 0; i < CERTAINTY_LEVELS; i++){
			levels[i] = Integer.toString(i);
		}
		certainty = new JSlider(JSlider.HORIZONTAL, 1, CERTAINTY_LEVELS, 1);
		certainty.setMajorTickSpacing(1);
		certainty.setMinorTickSpacing(1);
		certainty.setPaintTicks(true);
		certainty.setPaintLabels(true);
		certainty.addChangeListener(this);
		certainty.addKeyListener(this);
		certainty_val = new JTextField(1);
		certainty_val.addActionListener(this);
		status_panel.add(not_reviewed);
		status_panel.add(match);
		status_panel.add(not_match);
		not_reviewed.addKeyListener(this);
		match.addKeyListener(this);
		not_match.addKeyListener(this);
		JPanel cpanel = new JPanel();
		cpanel.add(certainty);
		cpanel.add(certainty_val);
		status_panel.add(cpanel);
		
		// add panels to MatchResultReviewPanel
		this.add(score_panel, BorderLayout.WEST);
		this.add(dem_panel, BorderLayout.CENTER);
		this.add(status_panel, BorderLayout.EAST);
		score_panel.addKeyListener(MatchResultReviewKeyboardAccelerator.INSTANCE);
	}
	
	public void setRow(int i){
		row.setText(Integer.toString(i));
	}
	
	public void setMatchResult(MatchResult mr){
		this.mr = mr;
		score.setText(Double.toString(mr.getScore()));
		demographics = mr.getDemographics().toArray(demographics);
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		Object[][] data = new Object[2][demographics.length];
		for(int i = 0; i < demographics.length; i++){
			data[0][i] = r1.getDemographic(demographics[i]);
			data[1][i] = r2.getDemographic(demographics[i]);
		}
		DemographicReviewTableModel drtm = new DemographicReviewTableModel(data, demographics);
		values.setModel(drtm);
		
		// set column widths
		int margin = 5;
		for (int i = 0; i < values.getColumnCount(); i++) {
            int vColIndex = i;
            TableColumnModel colModel  = values.getColumnModel();
            TableColumn col = colModel.getColumn(vColIndex);
            int width = 0;
            
            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();
            
            if (renderer == null) {
                renderer = values.getTableHeader().getDefaultRenderer();
            }

            Component comp = renderer.getTableCellRendererComponent(values, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
            
            // Get maximum width of column data
            for (int r = 0; r < values.getRowCount(); r++) {
                renderer = values.getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(values, values.getValueAt(r, vColIndex), false, false,r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2 * margin;

            // Set the width
            col.setPreferredWidth(width);
        }
		
		int status = mr.getMatch_status();
		switch(status){
		case MatchResult.MATCH:
			match.setSelected(true);
			break;
		case MatchResult.NON_MATCH:
			not_match.setSelected(true);
			break;
		default:
			not_reviewed.setSelected(true);
		}
		double c = mr.getCertainty();
		int display_certainty = getUICertaintyLevel(c);
		certainty.setValue(display_certainty);
		certainty_val.setText(Integer.toString(display_certainty));
		note.setText(mr.getNote());
	}
	
	private int getUICertaintyLevel(double certainty){
		int ret = (int) Math.round(( certainty * (CERTAINTY_LEVELS - 1) ) + 1);
		return ret;
	}
	
	private double getMatchResultCertainty(int certainty){
		double ret = (double)(certainty - 1) / (double)(CERTAINTY_LEVELS - 1);
		return ret;
	}
	
	public void setAsMatch(){
		match.doClick();
	}
	
	public void setAsNonMatch(){
		not_match.doClick();
	}
	
	public void setAsNotReviewed(){
		not_reviewed.doClick();
	}
	
	public void setGUICertainty(int c){
		if(c > 0 && c <= CERTAINTY_LEVELS){
			if(mr != null){
				mr.setCertainty(getMatchResultCertainty(c));
			}
			certainty_val.setText(Integer.toString(c));
			certainty.setValue(c);
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object s = ae.getSource();
		if(s instanceof JTextField){
			JTextField source = (JTextField)s;
			try{
				int new_val = Integer.parseInt(source.getText());
				if(new_val < 1){
					new_val = 1;
				} else if(new_val > CERTAINTY_LEVELS){
					new_val = CERTAINTY_LEVELS;
				}
				certainty.setValue(new_val);
				certainty_val.setText(Integer.toString(new_val));
				double mr_certainty =(double)new_val / CERTAINTY_LEVELS;
				if(mr != null){
					mr.setCertainty(mr_certainty);
				}
			}
			catch(NumberFormatException nfe){
				// invalid value in TextField, don't assign anything
			}
		} else if(s instanceof JRadioButton){
			if(mr != null){
				if(s == not_reviewed){
					mr.setMatch_status(MatchResult.UNKNOWN);
				} else if(s == match){
					mr.setMatch_status(MatchResult.MATCH);
				} else if(s == not_match){
					mr.setMatch_status(MatchResult.NON_MATCH);
				}
			}
			
		} else {
			String action_command = ae.getActionCommand();
			if(action_command.equals(MATCH_STRING)){
				match.setSelected(true);
			} else if(action_command.equals(NONMATCH_STRING)){
				not_match.setSelected(true);
			} else if(action_command.equals(NOT_REVIEWED_STRING)){
				not_reviewed.setSelected(true);
			}
			
		}
		
	}

	public void stateChanged(ChangeEvent ce) {
		Object s = ce.getSource();
		if(s instanceof JSlider){
			JSlider source = (JSlider)s;
			if(!source.getValueIsAdjusting()){
				int c = source.getValue();
				// set c to certainty_val, convert to double to MatchResult object value
				certainty_val.setText(Integer.toString(c));
				if(mr != null){
					mr.setCertainty(getMatchResultCertainty(c));
				}
			}
		}
		
	}

	public void keyPressed(KeyEvent arg0) {
		
	}

	public void keyReleased(KeyEvent arg0) {
		
	}

	public void keyTyped(KeyEvent arg0) {
		//System.out.println("key type event: " + arg0);
		if(mr != null){
			if(arg0.getSource() == note){
				mr.setNote(note.getText());
			} else if(arg0.getSource() == certainty){
				try{
					int c = Integer.parseInt(Character.toString(arg0.getKeyChar()));
					if(c > 0 && c <= CERTAINTY_LEVELS){
						mr.setCertainty(getMatchResultCertainty(c));
						certainty_val.setText(Integer.toString(c));
						certainty.setValue(c);
					}
				}
				catch(NumberFormatException nfe){
					
				}
			} else {
				// set radio buttons or slider
				if(arg0.getKeyCode() == KeyEvent.VK_M){
					setAsMatch();
				} else if(arg0.getKeyCode() == KeyEvent.VK_N){
					setAsNonMatch();
				} else if(arg0.getKeyCode() == KeyEvent.VK_R){
					setAsNotReviewed();
				}
			}
		}
	}
}
