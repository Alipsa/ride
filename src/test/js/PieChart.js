load("fx:controls.js")

pie=new PieChart();
pie.getData().clear();
pieData=FXCollections.observableArrayList();
pieData.add(new PieChart.Data("Sandwiches", 150));
pieData.add(new PieChart.Data("Salad", 90));
pieData.add(new PieChart.Data("Soup", 155));
pieData.add(new PieChart.Data("Beverages", 210));
pieData.add(new PieChart.Data("Desserts", 400));
pie.setData(pieData);
pie.setAnimated(true);
pie.setTitle("Lunch Sales");

inout.display(pie, "pie chart");