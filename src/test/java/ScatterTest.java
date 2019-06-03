import com.github.abel533.echarts.AxisPointer;
import com.github.abel533.echarts.Tooltip;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.LineType;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Tool;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.series.Scatter;
import com.github.abel533.echarts.style.LineStyle;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class ScatterTest {
    @Test
    public void test() {
        //地址：http://echarts.baidu.com/doc/example/scatter3.html
        EnhancedOption option = new EnhancedOption();
        option.tooltip(new Tooltip()
                .trigger(Trigger.axis)
                .showDelay(0)
                .axisPointer(new AxisPointer().type(PointerType.cross)
                        .lineStyle(new LineStyle()
                                .type(LineType.dashed).width(1))));
        option.legend("sin", "cos");
        option.toolbox().show(true).feature(Tool.mark, Tool.dataZoom, Tool.dataView, Tool.restore, Tool.saveAsImage);
        ValueAxis valueAxis = new ValueAxis().power(1).precision(2).scale(true);
        option.xAxis(valueAxis);
        option.yAxis(valueAxis);

        Scatter sin = new Scatter("sin");
        sin.large(true);
        Double[][] sinData = new Double[100][2];
        for (int i = sinData.length; i > 0; i--) {
            double x = round(Math.random() * 10) - 0;
            double y = Math.sin(x) - x * (i % 2 == 0 ? 0.1 : -0.1) * round(Math.random()) - 0;
            sinData[sinData.length - i] = new Double[]{x, y};
        }
        sin.data(sinData);

        Scatter cos = new Scatter("cos");
        cos.large(true);
        Double[][] cosData = new Double[100][2];
        for (int i = cosData.length; i > 0; i--) {
            double x = round(Math.random() * 10) - 0;
            double y = Math.cos(x) - x * (i % 2 == 0 ? 0.1 : -0.1) * round(Math.random()) - 0;
            cosData[sinData.length - i] = new Double[]{x, y};
        }
        cos.data(cosData);
        option.series(sin, cos);
        option.exportToHtml("scatter3.html");
        option.view();
    }

    public Double round(Double d) {
        BigDecimal bigDecimal = new BigDecimal(d.toString());
        bigDecimal = bigDecimal.round(new MathContext(3, RoundingMode.HALF_UP));
        return bigDecimal.doubleValue();
    }
}
