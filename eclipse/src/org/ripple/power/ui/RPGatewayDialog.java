package org.ripple.power.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONObject;
import org.ripple.power.config.LSystem;
import org.ripple.power.txns.AccountFind;
import org.ripple.power.txns.AccountLine;
import org.ripple.power.txns.Gateway;
import org.ripple.power.txns.IssuedCurrency;
import org.ripple.power.txns.Rollback;
import org.ripple.power.txns.TrustSet;
import org.ripple.power.txns.Updateable;
import org.ripple.power.wallet.WalletItem;

public class RPGatewayDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private WalletItem _item = null;

	final private int max_trust = 10000000;

	// Variables declaration - do not modify
	private RPCButton _addGatewayButton;
	private RPCButton _manageGatewayButton;
	private RPCButton _exitButton;
	private RPCButton _cancelTrustButton;
	private RPCButton _okTrustButton;
	private RPCButton _createGatewayButton;
	private RPComboBox _curList;
	private RPLabel jLabel1;
	private RPLabel jLabel2;
	private RPLabel jLabel3;
	private RPLabel jLabel4;
	private RPLabel jLabel5;
	private RPLabel jLabel6;
	private RPLabel jLabel7;
	private RPList _ioulistTable;
	private RPList _listGateway;
	private RPList _myGateway;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSlider _trustlimits;
	private RPTextBox _addressText;
	private RPTextBox _trustValueText;

	// End of variables declaration

	private ArrayList<String> _iouList = new ArrayList<String>(100);

	public static RPGatewayDialog showDialog(String text, JFrame parent,
			final WalletItem item) {
		RPGatewayDialog dialog = new RPGatewayDialog(text, parent, item);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return dialog;
	}

	public RPGatewayDialog(String text, JFrame parent, final WalletItem item) {
		super(parent, text, Dialog.ModalityType.DOCUMENT_MODAL);
		this._item = item;
		setResizable(false);
		Dimension dim = new Dimension(780, 610);
		setPreferredSize(dim);
		setSize(dim);
		initComponents();

	}

	private void initComponents() {
		getContentPane().setBackground(new Color(36, 36, 36));
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane2 = new javax.swing.JScrollPane();
		jScrollPane3 = new javax.swing.JScrollPane();

		_ioulistTable = new RPList();
		_listGateway = new RPList();
		_myGateway = new RPList();

		jSeparator1 = new javax.swing.JSeparator();
		jLabel1 = new RPLabel();
		jPanel1 = new javax.swing.JPanel();
		jLabel2 = new RPLabel();
		jLabel3 = new RPLabel();
		jLabel4 = new RPLabel();
		jLabel5 = new RPLabel();
		jLabel6 = new RPLabel();
		jLabel7 = new RPLabel();

		_trustlimits = new javax.swing.JSlider();

		_addressText = new RPTextBox();
		_curList = new RPComboBox();

		_addGatewayButton = new RPCButton();
		_manageGatewayButton = new RPCButton();
		_exitButton = new RPCButton();
		_cancelTrustButton = new RPCButton();
		_okTrustButton = new RPCButton();
		_createGatewayButton = new RPCButton();

		getContentPane().setLayout(null);

		getContentPane().add(jSeparator1);
		jSeparator1.setBounds(0, 520, 781, 10);

		jLabel1.setText("网关列表");
		getContentPane().add(jLabel1);
		jLabel1.setBounds(10, 10, 170, 20);

		jPanel1.setBackground(new java.awt.Color(51, 51, 51));
		jPanel1.setLayout(null);

		jLabel2.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
		jLabel2.setForeground(new java.awt.Color(255, 255, 255));
		jLabel2.setText("币种名称");
		jPanel1.add(jLabel2);
		jLabel2.setBounds(10, 70, 80, 16);

		jLabel3.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
		jLabel3.setForeground(new java.awt.Color(255, 255, 255));
		jLabel3.setText("网关地址");
		jPanel1.add(jLabel3);
		jLabel3.setBounds(10, 20, 91, 16);

		_trustlimits.setBackground(new java.awt.Color(51, 51, 51));
		_trustlimits.setBounds(90, 120, 480, 23);
		_trustlimits.setMinimum(0);
		_trustlimits.setMaximum(max_trust);
		_trustlimits.setValue(max_trust);
		_trustlimits.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Object src = e.getSource();
				if (src instanceof JSlider) {
					double percentage = _trustlimits.getValue();
					int max = _trustlimits.getMaximum();
					int min = _trustlimits.getMinimum();
					int value = (int) (min + (percentage * (max - min))
							/ max_trust);
					_trustValueText.setText(String.valueOf(value));
				}

			}
		});
		jPanel1.add(_trustlimits);

		_trustValueText = new RPTextBox();
		_trustValueText.setBounds(90, 170, 200, 21);
		_trustValueText.setText(String.valueOf(max_trust));
		jPanel1.add(_trustValueText);

		jLabel5.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
		jLabel5.setForeground(new java.awt.Color(255, 255, 255));
		jLabel5.setText("信任额度");
		jPanel1.add(jLabel5);
		jLabel5.setBounds(10, 120, 80, 16);
		jPanel1.add(_addressText);
		_addressText.setBounds(90, 20, 478, 21);

		jLabel6.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
		jLabel6.setForeground(new java.awt.Color(255, 255, 255));
		jLabel6.setText("具体金额");
		jPanel1.add(jLabel6);
		jLabel6.setBounds(10, 170, 80, 16);

		jLabel7.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
		jLabel7.setForeground(new java.awt.Color(255, 255, 255));
		jLabel7.setText("我的信任");
		jPanel1.add(jLabel7);
		jLabel7.setBounds(10, 220, 80, 16);

		_curList.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"CNY", "BTC", "USD", "JPY" }));
		jPanel1.add(_curList);
		_curList.setBounds(90, 70, 130, 21);

		getContentPane().add(jPanel1);
		jPanel1.setBounds(190, 10, 580, 500);

		jLabel4.setText("IOU发行");
		getContentPane().add(jLabel4);
		jLabel4.setBounds(10, 280, 130, 15);

		final String[] gatewaystrings = Gateway.gatewayList();

		_listGateway.setModel(new javax.swing.AbstractListModel() {

			public int getSize() {
				return gatewaystrings.length;
			}

			public Object getElementAt(int i) {
				return gatewaystrings[i];
			}
		});
		_listGateway.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				RPList list = (RPList) e.getSource();
				String name = (String) list.getSelectedValue();
				synchronized (_iouList) {
					if (Gateway.getAddress(name) != null) {
						_iouList.clear();
						ArrayList<Gateway.Item> items = Gateway
								.getAddress(name).accounts;
						for (int i = 0; i < items.size(); i++) {
							_iouList.addAll(items.get(i).currencies);

						}
						_ioulistTable.updateUI();
						_curList.setModel(new javax.swing.DefaultComboBoxModel(
								_iouList.toArray()));
						if (Gateway.getAddress(name).accounts.size() > 0) {
							_addressText.setText(Gateway.getAddress(name).accounts
									.get(0).address);
						}

					}
				}

			}
		});
		jScrollPane2.setViewportView(_listGateway);
		_listGateway.setSelectedIndex(0);

		getContentPane().add(jScrollPane2);
		jScrollPane2.setBounds(10, 40, 170, 230);

		_ioulistTable.setModel(new javax.swing.AbstractListModel() {

			public int getSize() {
				return _iouList.size();
			}

			public Object getElementAt(int i) {
				return _iouList.get(i);
			}
		});

		jScrollPane1.setViewportView(_ioulistTable);

		getContentPane().add(jScrollPane1);
		jScrollPane1.setBounds(10, 310, 170, 200);

		_ioulistTable.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				RPList list = (RPList) e.getSource();
				String name = (String) list.getSelectedValue();
				_curList.setSelectedItem(name);

			}
		});

		_myGateway.setModel(new javax.swing.AbstractListModel() {

			String[] list = new String[] { "Empty" };

			public int getSize() {
				return list.length;
			}

			public Object getElementAt(final int i) {
				return list[0];
			}
		});

		jScrollPane3.setViewportView(_myGateway);

		jPanel1.add(jScrollPane3);

		jScrollPane3.setBounds(90, 220, 480, 200);

		_addGatewayButton.setText("添加网关");
		getContentPane().add(_addGatewayButton);
		_addGatewayButton.setBounds(420, 540, 80, 30);

		_manageGatewayButton.setText("管理网关");
		getContentPane().add(_manageGatewayButton);
		_manageGatewayButton.setBounds(120, 540, 100, 30);

		_exitButton.setText("Exit");
		getContentPane().add(_exitButton);
		_exitButton.setBounds(690, 540, 80, 30);

		_cancelTrustButton.setText("取消信任");
		getContentPane().add(_cancelTrustButton);
		_cancelTrustButton.setBounds(600, 540, 80, 30);

		_okTrustButton.setText("确定信任");
		getContentPane().add(_okTrustButton);
		_okTrustButton.setBounds(510, 540, 80, 30);
		_okTrustButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String address = _addressText.getText().trim();

				String curName = (String) _curList.getSelectedItem();

				String trustValue = _trustValueText.getText().trim();

				String message = String.format(
						"您希望信任网关%s\n的货币%s,并且设置信任额度为%s吗?", address, curName,
						trustValue);

				int result = RPMessage.showConfirmMessage(
						LSystem.applicationMain, "信任网关", message, "确认", "放弃");
				if (result == 0) {
					final WaitDialog dialog = new WaitDialog(
							RPGatewayDialog.this);
					IssuedCurrency cur = new IssuedCurrency(trustValue,
							address, curName);
					TrustSet.set(_item.getSeed(), cur, LSystem.FEE,
							new Rollback() {

								@Override
								public void success(JSONObject res) {
									JSonLog.get().println(res.toString());
									dialog.closeDialog();
									String result = res.getJSONObject("result")
											.getString("engine_result_message");
									if (result != null) {
										RPMessage.showInfoMessage(
												LSystem.applicationMain,
												"Info", "操作被处理,Rippled反馈:"
														+ result);
										loadTrust();
									}
								}

								@Override
								public void error(JSONObject res) {
									JSonLog.get().println(res.toString());
									dialog.closeDialog();
									RPMessage.showErrorMessage(
											LSystem.applicationMain, "Error",
											"信任失败.");
								}
							});
				}
			}
		});

		_createGatewayButton.setText("创建网关");
		getContentPane().add(_createGatewayButton);
		_createGatewayButton.setBounds(10, 540, 100, 30);

		emptyAddress();
		loadTrust();
		pack();

	}// </editor-fold>

	private void loadTrust() {
		if (_item != null) {
			final WaitDialog dialog = WaitDialog
					.showDialog(RPGatewayDialog.this);
			AccountFind.getTrusts(_item.getPublicKey(), new Updateable() {
				@Override
				public void action(Object o) {
					if (o != null) {
						if (o instanceof ArrayList) {
							final ArrayList<IssuedCurrency> lines = (ArrayList<IssuedCurrency>) o;
							_myGateway
									.setModel(new javax.swing.AbstractListModel() {

										public int getSize() {
											return lines.size();
										}

										public Object getElementAt(int i) {
											String mes = null;
											IssuedCurrency cur = lines.get(i);
											if (cur == null) {
												return "Empty";
											}
											if (cur.tag != null) {
												mes = lines.get(i)
														.toGatewayString()
														+ " Limit:"
														+ ((AccountLine) lines
																.get(i).tag)
																.getLimit();
											} else {
												mes = lines.get(i)
														.toGatewayString();
											}
											return mes;
										}
									});
						}

					}
					dialog.closeDialog();
				}
			});
		}
	}

	private void emptyAddress() {
		if (_item == null || "0".equals(_item.getAmount())
				|| "0.000000".equals(_item.getAmount())) {
			_myGateway.setEnabled(false);
			_createGatewayButton.setEnabled(false);
			_manageGatewayButton.setEnabled(false);
			_addGatewayButton.setEnabled(false);
			_cancelTrustButton.setEnabled(false);
			_okTrustButton.setEnabled(false);
		}
	}
}