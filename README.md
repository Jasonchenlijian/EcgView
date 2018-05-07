# EcgView
Android版本的自定义心电图View。

>这是一个通过常规的自定义View实现的心电图样式。主要是背景加心电数据线两方面。
>心电图的样式有很多种，此方案提供了实现方式的参考，可以通过借鉴源码自行修改。
>在这个View的绘制过程中，需要注意View尺寸大小和padding的处理，以及固定的宽高比，图形的居中、缩放、边距等方面。


# Preview
![效果图](https://github.com/Jasonchenlijian/EcgView/raw/master/preview/preview.png) 


## Usage

- 在xml添加View，并设置对应属性

    	<com.clj.ecgview.EcgView
        	android:id="@+id/ecg_view"
        	android:layout_width="match_parent"
        	android:layout_height="360dp"
        	app:backgroundColor="@android:color/white"
        	app:gridColor="@android:color/holo_red_dark"
        	app:intervalColumn="6"
        	app:lineColor="@android:color/black"
        	app:lines="4"
        	app:littleGrid="true"
        	app:minGridNum="5"
        	app:mvData="4.25f"
        	app:scaleType="scaleCenter"
        	app:startColumn="4"
        	app:totalSize="4800"
        	app:xGridNum="26"
        	app:yGridNum="38" />
     

- xml属性说明

    	<declare-styleable name="EcgView">
        	<!--显示模式，左上角还是居中-->
        	<attr name="scaleType">
            	<enum name="normal" value="0" />
           	 <enum name="scaleCenter" value="1" />
        	</attr>
        	<!--X轴大网格个数-->
        	<attr name="xGridNum" format="integer" />
        	<!--Y轴大网格个数-->
        	<attr name="yGridNum" format="integer" />
        	<!--每个大网格中的小网格个数-->
        	<attr name="minGridNum" format="integer" />
        	<!--是否绘制小网格-->
        	<attr name="littleGrid" format="boolean" />
        	<!--背景颜色-->
        	<attr name="backgroundColor" format="color|integer" />
        	<!--大网格颜色-->
        	<attr name="gridColor" format="color|integer" />
        	<!--小网格颜色-->
        	<attr name="littleGridColor" format="color|integer" />
        	<!--心电线的颜色-->
        	<attr name="lineColor" format="color|integer" />
        	<!--心电数据总数-->
        	<attr name="totalSize" format="integer" />
        	<!--最左边线间隔大网格数-->
        	<attr name="startColumn" format="integer" />
        	<!--每两条线之间间隔大网格数-->
        	<attr name="intervalColumn" format="integer" />
        	<!--每个小网格达标的心电值-->
        	<attr name="mvData" format="float" />
    	</declare-styleable>

- 在代码给心电图设置数据即可

		EcgView ecgView = findViewById(R.id.ecg_view);
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 4800; i++) {
            list.add(random.nextInt(30));
        }
        ecgView.setDataList(list);
 
	 


