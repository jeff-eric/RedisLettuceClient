/**
 *
 */
package cn.org.tpeach.nosql.view.dialog;

import cn.org.tpeach.nosql.controller.BaseController;
import cn.org.tpeach.nosql.controller.ResultRes;
import cn.org.tpeach.nosql.redis.bean.RedisKeyInfo;
import cn.org.tpeach.nosql.redis.service.IRedisConnectService;
import cn.org.tpeach.nosql.service.ServiceProxy;
import cn.org.tpeach.nosql.tools.StringUtils;
import cn.org.tpeach.nosql.tools.SwingTools;
import cn.org.tpeach.nosql.view.component.PlaceholderTextField;
import cn.org.tpeach.nosql.view.component.RTextArea;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * <p>
 * Title: AddRowDialog.java</p>
 *
 * @author taoyz
 * @date 2019年9月4日
 * @version 1.0
 */
public class AddRowDialog extends AbstractRowDialog<RedisKeyInfo, RedisKeyInfo> {

    /**
     *
     */
    private static final long serialVersionUID = 2648311460220989594L;
    private JPanel panel, textAreaPanel, scorePanel;
    private JLabel fieldHashLabel, scoreLable, valueLabel, valueHashLabel;
    private PlaceholderTextField scoreField;
    private RTextArea valueArea, valueHashArea, hashKeyArea;
    IRedisConnectService redisConnectService = ServiceProxy.getBeanProxy("redisConnectService", IRedisConnectService.class);
    @Setter
    private boolean isLeftList;
    /**
     * @param parent
     * @param t
     */
    public AddRowDialog(JFrame parent, RedisKeyInfo t) {
        super(parent, t);

    }

    @Override
    public void initDialog(RedisKeyInfo t) {
        this.setTitle("添加行");
        if (t == null) {
            this.isError = true;
        }

    }
    @Override
	public void setMinimumSize() {

        super. setMinimumSize();
    }
    @Override
    protected void contextUiImpl(JPanel contextPanel, JPanel btnPanel) {
        super.contextUiImpl(contextPanel, btnPanel);
        panel = new JPanel();
        scoreLable = new JLabel("数据分数:", JLabel.RIGHT);
        valueLabel = new JLabel("数据键值:", JLabel.RIGHT);
        valueHashLabel = new JLabel("散列键值:", JLabel.RIGHT);
        fieldHashLabel = new JLabel("散列字段:", JLabel.RIGHT);

        scoreField = new PlaceholderTextField(20);
        scoreField.setPlaceholder("score");
        hashKeyArea = new RTextArea(3, 20);
        hashKeyArea.setLineWrap(true);
        valueHashArea = new RTextArea(3, 20);
        valueHashArea.setLineWrap(true);

        switch (t.getType()) {
            case STRING:
                break;
            case LIST:
            case SET:
                valueArea = new RTextArea(8, 20);
                valueArea.setLineWrap(true);
                textAreaPanel = SwingTools.createTextRow(valueLabel, valueArea.getJScrollPane(), 0.2, 0.8, this.getWidth(), rowHeight * 7, null, new Insets(0, 0, 0, 0), new Insets(0, 10, 0, 30));
                panel.add(textAreaPanel);
                break;
            case HASH:
                panel.add(SwingTools.createTextRow(fieldHashLabel, hashKeyArea.getJScrollPane(), this.getWidth(), (int) (rowHeight * 3)));
                JPanel valueHashAreaPanel =  SwingTools.createTextRow(valueHashLabel, valueHashArea.getJScrollPane(), 0.3, 0.7, this.getWidth(), (int) (rowHeight * 3), null, new Insets(13, 10, 0, 0), new Insets(13, 10, 0, 30));
                panel.add(valueHashAreaPanel);
                break;
            case ZSET:
                valueArea = new RTextArea(6, 20);
                scorePanel = SwingTools.createTextRow(scoreLable, scoreField, this.getWidth(), rowHeight);
                panel.add(scorePanel);
                textAreaPanel = SwingTools.createTextRow(valueLabel, valueArea.getJScrollPane(), this.getWidth(), rowHeight * 5);
                panel.add(textAreaPanel);

                break;
            default:
                break;
        }

//		hashTextAreaPanel = (JPanel) createHashTextAreaRow(valueHashLabel, hashKeyField,valueHashArea.getJScrollPane());
//		panel.add(hashTextAreaPanel);
        contextPanel.setLayout(new BorderLayout());
        contextPanel.add(panel, BorderLayout.CENTER);
    }

    @Override
    protected void submit(ActionEvent e) {
        if (consumer == null) {
            SwingTools.showMessageErrorDialog(this, "未绑定回调事件");
            return;
        }
        switch (t.getType()) {
            case ZSET:
                if (StringUtils.isBlank(scoreField.getText())) {
                    SwingTools.showMessageErrorDialog(this, "请输入分数");
                    return;
                }
                try {
                    t.setScore(Double.valueOf(scoreField.getText()));
                } catch (NumberFormatException ez) {
                    SwingTools.showMessageErrorDialog(this, "请输入正确的分数");
                    return;
                }
            case STRING:
            case LIST:
            case SET:
                if (StringUtils.isBlank(valueArea.getText())) {
                    SwingTools.showMessageErrorDialog(this, "请输入键值");
                    return;
                }
                t.setValue(StringUtils.strToByte(valueArea.getText()));
                break;
            case HASH:
                if (StringUtils.isBlank(valueHashArea.getText())) {
                    SwingTools.showMessageErrorDialog(this, "请输入键值");
                    return;
                }
                if (StringUtils.isBlank(hashKeyArea.getText())) {
                    SwingTools.showMessageErrorDialog(this, "请输入Field");
                    return;
                }
                t.setField(StringUtils.strToByte(hashKeyArea.getText()));
                t.setValue(StringUtils.strToByte(valueHashArea.getText()));
                break;
            default:
                break;
        }
        ResultRes<?> res = BaseController.dispatcher(() -> redisConnectService.addRowKeyInfo(t,isLeftList));
        if (res.isRet()) {
            consumer.accept(t);
            this.dispose();
        } else {
            SwingTools.showMessageErrorDialog(this, res.getMsg());
        }
    }


	@Override
	public boolean isNeedBtn() {
		return true;
	}

}
