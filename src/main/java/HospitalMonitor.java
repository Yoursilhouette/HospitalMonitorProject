import HES.Systems.EnvironmentalSystem;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.Color;

public class HospitalMonitor extends JFrame {
    private EnvironmentalSystem system = new EnvironmentalSystem();
    // JLabel 是窗口中用于显示文字的组件
    private JLabel northLabel = new JLabel();
    private JLabel centralLabel = new JLabel();
    private JLabel southLabel = new JLabel();
    private JLabel humidityLabel = new JLabel();
    private JLabel pollutionLabel = new JLabel();
    private JPanel northPanel = new JPanel();
    private JPanel centralPanel = new JPanel();
    private JPanel southPanel = new JPanel();
    private JPanel humidityPanel = new JPanel();
    private JPanel pollutionPanel = new JPanel();
    private int secondsPassed = 0;

    public HospitalMonitor() {
        setTitle("Hospital Monitor");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
         * 五行、一列。
         * 每个格子之间留一点空隙。
         */
        setLayout(new GridLayout(5, 1, 8, 8));

        // 让每个 JLabel 的文字水平居中
        northLabel.setHorizontalAlignment(JLabel.CENTER);
        centralLabel.setHorizontalAlignment(JLabel.CENTER);
        southLabel.setHorizontalAlignment(JLabel.CENTER);
        humidityLabel.setHorizontalAlignment(JLabel.CENTER);
        pollutionLabel.setHorizontalAlignment(JLabel.CENTER);

        // 每个 JLabel 放进自己的 JPanel 格子
        northPanel.add(northLabel);
        centralPanel.add(centralLabel);
        southPanel.add(southLabel);
        humidityPanel.add(humidityLabel);
        pollutionPanel.add(pollutionLabel);

        // 将五个格子加入 JFrame
        add(northPanel);
        add(centralPanel);
        add(southPanel);
        add(humidityPanel);
        add(pollutionPanel);


        setLocationRelativeTo(null);
        setVisible(true);

        updateSystem();

        Timer timer = new Timer(1000, e -> updateSystem());
        timer.start();
    }


    private void updateSystem () {

        // 1. 读取三个病区现在的温度
        float northTemperature =
                system.getTempFeed().getTemperature(0);

        float centralTemperature =
                system.getTempFeed().getTemperature(1);

        float southTemperature =
                system.getTempFeed().getTemperature(2);

        // 2. 根据温度控制暖气
        controlHeating(0, northTemperature, 23.0f);
        controlHeating(1, centralTemperature, 22.0f);
        controlHeating(2, southTemperature, 20.0f);

        // 3. 取得三个暖气目前的状态
        boolean northHeatingOn = system.isHeatingOn(0);
        boolean centralHeatingOn = system.isHeatingOn(1);
        boolean southHeatingOn = system.isHeatingOn(2);

        // 4. 更新三个 Ward 的 JLabel 文字
        northLabel.setText(String.format(
                "<html><b>North Ward</b><br>"
                        + "Current: %.1f °C<br>"
                        + "Ideal: 23.0 °C<br>"
                        + "Heater: %s</html>",
                northTemperature,
                northHeatingOn ? "ON" : "OFF"
        ));

        centralLabel.setText(String.format(
                "<html><b>Central Ward</b><br>"
                        + "Current: %.1f °C<br>"
                        + "Ideal: 22.0 °C<br>"
                        + "Heater: %s</html>",
                centralTemperature,
                centralHeatingOn ? "ON" : "OFF"
        ));

        southLabel.setText(String.format(
                "<html><b>South Ward</b><br>"
                        + "Current: %.1f °C<br>"
                        + "Ideal: 20.0 °C<br>"
                        + "Heater: %s</html>",
                southTemperature,
                southHeatingOn ? "ON" : "OFF"
        ));

        // 5. 若温度不在允许范围内，将相应格子变红
        setTemperatureWarning(northPanel, northTemperature, 23.0f);
        setTemperatureWarning(centralPanel, centralTemperature, 22.0f);
        setTemperatureWarning(southPanel, southTemperature, 20.0f);
        // ---------- Humidity ----------

// 读取室内和室外湿度
        int insideHumidity =
                system.getHumidityFeed().getInsideHumidity();

        int outsideHumidity =
                system.getHumidityFeed().getOutsideHumidity();

// 计算：室内湿度 - 室外湿度
        int humidityDifference = insideHumidity - outsideHumidity;

// 差值超过 10%：开启除湿机
        if (humidityDifference > 10) {
            system.turnDehumidifierOn(true);
            humidityPanel.setBackground(Color.RED);
        } else {
            system.turnDehumidifierOn(false);
            humidityPanel.setBackground(Color.WHITE);
        }

// 读取除湿机目前是否开启
        boolean dehumidifierOn = system.isDehumidifierOn();


        humidityLabel.setText(String.format("<html><b>Humidity</b><br>"
                        + "Inside: %d%%<br>"
                        + "Inside: %d%%<br>"
                        + "Difference: %d%%<br>"
                        + "Dehumidifier: %s</html>",
                insideHumidity,
                outsideHumidity,
                humidityDifference,
                dehumidifierOn ? "ON" : "OFF"));
        // ---------- PM2.5 ----------
        float pollution = system.getPollutionFeed().getPollution();
        if (pollution > 5.0f) {
            system.turnAirPurifierOn(true);
            pollutionPanel.setBackground(Color.RED);
        }
        else { system.turnAirPurifierOn(false);
            pollutionPanel.setBackground(Color.WHITE);
        }
        boolean airPurifierOn = system.isAirPurifierOn();

        pollutionLabel.setText(String.format("<html><b>Pollution</b><br>"
                        + "Current: %.1f<br>"
                        + "AirPurifier: %s</html>",
                pollution,
                airPurifierOn ? "ON" : "OFF"));

        // ---------- 30-second Console log ----------

        secondsPassed++;

        if (secondsPassed % 30 == 0) {
            System.out.println();
            System.out.println("========== 30-second system log ==========");

            System.out.printf(
                    "North Ward - Current: %.1f °C, Ideal: 23.0 °C, Heater: %s%n",
                    northTemperature,
                    northHeatingOn ? "ON" : "OFF"
            );

            System.out.printf(
                    "Central Ward - Current: %.1f °C, Ideal: 22.0 °C, Heater: %s%n",
                    centralTemperature,
                    centralHeatingOn ? "ON" : "OFF"
            );

            System.out.printf(
                    "South Ward - Current: %.1f °C, Ideal: 20.0 °C, Heater: %s%n",
                    southTemperature,
                    southHeatingOn ? "ON" : "OFF"
            );

            System.out.printf(
                    "Humidity - Inside: %d%%, Outside: %d%%, Difference: %d%%, "
                            + "Dehumidifier: %s%n",
                    insideHumidity,
                    outsideHumidity,
                    humidityDifference,
                    dehumidifierOn ? "ON" : "OFF"
            );

            System.out.printf(
                    "PM2.5 Pollution - Current: %.1f, Air Purifier: %s%n",
                    pollution,
                    airPurifierOn ? "ON" : "OFF"
            );

            System.out.println("CT Scanner: "
                    + system.getCTScannerStatus());

            System.out.println("Ultrasound Scanner: "
                    + system.getUltrasoundScannerStatus());

            System.out.println("============================================");
        }
    }

        private void controlHeating(
        int wardId,
        float currentTemperature,
        float targetTemperature) {

            if (currentTemperature < targetTemperature - 0.5f) {
                system.turnHeatingOn(wardId, true);

            } else if (currentTemperature > targetTemperature + 0.5f) {
                system.turnHeatingOn(wardId, false);
            }
        }
        private void setTemperatureWarning(
                JPanel panel,
        float currentTemperature,
        float targetTemperature) {

            boolean temperatureOutOfRange =
                    currentTemperature < targetTemperature - 0.5f
                            || currentTemperature > targetTemperature + 0.5f;

            if (temperatureOutOfRange) {
                panel.setBackground(Color.RED);
            } else {
                panel.setBackground(Color.WHITE);
            }
        }


        public static void main (String[]args){
            SwingUtilities.invokeLater(() -> new HospitalMonitor());
        }
    }