package com.lapluma.knowledg.util;

import com.github.abel533.echarts.Label;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Layout;
import com.github.abel533.echarts.code.SeriesType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Graph;
import com.github.abel533.echarts.series.Line;
import com.github.abel533.echarts.series.Series;
import com.github.abel533.echarts.series.force.Category;
import com.github.abel533.echarts.series.force.Link;
import com.github.abel533.echarts.series.force.Node;
import com.github.abel533.echarts.series.other.Force;
import com.github.abel533.echarts.style.ItemStyle;
import com.github.abel533.echarts.style.itemstyle.Emphasis;
import com.lapluma.knowledg.model.RelatedInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchartOptionUtil {

    public static GsonOption getLineChartOptions(Object[] xAxis, Object[] yAxis) {
        GsonOption option = new GsonOption();
        option.title("折线图");
        option.legend("销量");
        option.tooltip().trigger(Trigger.axis);

        ValueAxis valueAxis = new ValueAxis();
        option.yAxis(valueAxis);

        CategoryAxis categorxAxis = new CategoryAxis();
        categorxAxis.axisLine().onZero(false);
        categorxAxis.boundaryGap(true);
        categorxAxis.data(xAxis);
        option.xAxis(categorxAxis);

        Line line = new Line();
        line.smooth(false).name("销量").data(yAxis).itemStyle().normal().lineStyle().shadowColor("rgba(0,0,0,0.4)");
        option.series(line);
        return option;
    }

    public static GsonOption getGraphOptions(String name, ArrayList<RelatedInstance> list) {
        GsonOption option = new GsonOption();

        option.legend("实体", "客体", "主体");
        // init graph
        Graph g = new Graph();
        g.setName(name);
        g.setType(SeriesType.graph);
        g.setLayout(Layout.force);
//        g.setRoam(true);
        // force
        Force force = new Force();
        force.setEdgeLength(100);
        force.setRepulsion(300);
        g.setForce(force);

        // categories
        Category centerInstance = new Category();
        centerInstance.setName("实体");
        Category objectInstance = new Category();
        objectInstance.setName("客体");
        Category subjectInstance = new Category();
        subjectInstance.setName("主体");
        g.setCategories(new ArrayList<Category>(Arrays.asList(centerInstance, objectInstance, subjectInstance)));

        // node label
        Label label = new Label();
        label.setShow(true);
        label.setPosition("top");

        // main node
        Node centerNode = new Node();
        centerNode.setCategory(0);
        centerNode.setName(name);
        System.out.println();
        centerNode.setSymbolSize(50);
        centerNode.put("id", String.valueOf(-1));
        centerNode.put("label", label);
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(centerNode);
        ArrayList<Link> links = new ArrayList<Link>();
        // related nodes
        for (RelatedInstance ri: list) {
            Node newNode = new Node();
            Link newLink = new Link();
            newNode.setName(ri.getLabel());
            newNode.put("id", String.valueOf(ri.getId()));
            newNode.put("label", label);
            newNode.setSymbolSize(25);
            if (ri.getRel().equals("object")) {
                newNode.setCategory(1);
                newLink.setTarget(String.valueOf(-1));
                newLink.setSource(String.valueOf(ri.getId()));
            }
            else {
                newNode.setCategory(2);
                newLink.setSource(String.valueOf(-1));
                newLink.setTarget(String.valueOf(ri.getId()));
            }
            nodes.add(newNode);
            links.add(newLink);
        }

        g.setData(nodes);
        g.setLinks(links);

        option.series(g);
//        System.out.println(option.toString()); // ONLY FOR DEBUGGING, REMOVE BEFORE RELEASE;
        return option;
    }
}
