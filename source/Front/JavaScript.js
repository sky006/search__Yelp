$(document).ready(function(){
  function info(Category,Address,Rate,URL,Name,Price,Picture,Distance,Menu){
    Rate = Rate * 20;
    $("#content").append('<div class="thumbnail">'+
                                '<div class="col-sm-4 col-md-4">'+
                                  '<img src="'+Picture+'" alt="" class="img-rounded" width="100" height="100">'+
                                    '<h4>Rating:</h4>'+
                                      '<div class="progress">'+
                                        '<div class="progress-bar" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: '+Rate+'%;">'+
                                        '</div>'+
                                      '</div>'+
                                '</div>'+
                                '<div class="caption">'+
                                  '<h3>'+Name+'</h3>'+
                                  '<p>Category:'+Category+'</p>'+
                                  '<p>Address:'+Address+'</p>'+
                                  '<a href='+URL+'">Webiste</a>'+
                                  //'<p>URL:'+URL+'</p>'+
                                  '<p>Price: '+Price+'</p>'+
                                  '<p>Distance: '+Distance+'</p>'+
                                  '<p>Menu: '+Menu+'</p>'+
                                '</div>'+
                            '</div>');
  }
  function infocommend(Category,Address,Rate,URL,Name,Price,Picture,Distance){
    Rate = Rate * 20;
    $("#recommendation").append('<div class="thumbnail">'+
                                '<div class="col-sm-4 col-md-4">'+
                                  '<img src="'+Picture+'" alt="" class="img-rounded" width="100" height="100">'+
                                    '<h4>Rating:</h4>'+
                                      '<div class="progress">'+
                                        '<div class="progress-bar" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: '+Rate+'%;">'+
                                        '</div>'+
                                      '</div>'+
                                '</div>'+
                                '<div class="caption">'+
                                  '<h3>'+Name+'</h3>'+
                                  '<p>Category:'+Category+'</p>'+
                                  '<p>Address:'+Address+'</p>'+
                                  '<a href='+URL+'">Webiste</a>'+
                                   //'<p>URL:'+URL+'</p>'+
                                  '<p>Price: '+Price+'</p>'+
                                  '<p>Distance: '+Distance+'</p>'+
                                  //'<p>Menu: '+Menu+'</p>'+
                                '</div>'+
                            '</div>');
  }
  function addPage(currentPage){$('#currentPageNumber').text(currentPage);}
  
  $("#search-button").click(function(){
        $('#content').html("");
        $('#recommendation').html("");
        var engine = '';
        $('#example-navbar-collapse .active').each(function(){
          engine = $(this).attr('id');
        });
        if (engine == "hadoop-button"){engine = "Hadoop";}
        if (engine == "lucene-button"){engine = "Lucene";}
        var pageNum = '1';
        var sort = '';
        sort = $('#orderoption').val();
        if(sort == 'Order By Default'){sort = 'Default';}
        if(sort == 'Order By Rate'){sort = 'Rate';}
        if(sort == 'Order By Distance'){sort = 'Distance';}
        var content = $('#query').val();
        var city = $('#city').val();
        //alert(' Content:'+ content + ' City:' + city + 'Engine: ' + engine + 'pagenumber:' + pageNum);
        addPage(pageNum);
        //alert($('#example').text());
        $.ajax({
          url: 'http://localhost:8080/Search/Main',//
          data:{
            pageNum:pageNum,
            indexType:engine,
            keywords:content,
            location:city,
            orderType:sort
          },
          type: "post",
          dataType: "json",
          success:function(data){
             if(data.numResults != 0){addPage();}
             $('#content').html("");
             $('#recommendation').html("");
             $('#content').html("<h3><center>Result</center></h3>");
             $('#recommendation').html("<h3><center>Recommendation</center></h3>");
             $('#slogan').html("");
            $.each(data.results,function(i,item){
              info(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance,item.Menu);
            });
            $.each(data.recommend,function(i,item){
              //alert(item.Distance);
              infocommend(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance);
            });
          },
          error: function(XMLHttpeRequest, textStatus, errorThrown){
            alert("Error!");
          }
        });
   });

  $("#Previous").click(function(){
        $('#content').html("");
        $('#recommendation').html("");
        var engine = '';
        $('#example-navbar-collapse .active').each(function(){
          engine = $(this).attr('id');
        });
        if (engine == "hadoop-button"){engine = "Hadoop";}
        if (engine == "lucene-button"){engine = "Lucene";}
        var pageNum = $('#currentPageNumber').text();
        var temp = Number(pageNum);
        if (temp > 1){temp = temp - 1;}
        //temp = temp - 1;
        pageNum = String(temp);
        $('#currentPageNumber').text(pageNum);
        var sort = '';
        sort = $('#orderoption').val();
        if(sort == 'Order By Default'){sort = 'Default';}
        if(sort == 'Order By Rate'){sort = 'Rate';}
        if(sort == 'Order By Distance'){sort = 'Distance';}
        var content = $('#query').val();
        var city = $('#city').val();
        //alert(' Content:'+ content + ' City:' + city + 'Engine: ' + engine + 'pagenumber:' + pageNum);
        addPage(pageNum);
        //alert($('#example').text());
        $.ajax({
          url: 'http://localhost:8080/Search/Main',//
          data:{
            pageNum:pageNum,
            indexType:engine,
            keywords:content,
            location:city,
            orderType: sort
          },
          type: "post",
          dataType: "json",
          success:function(data){
             if(data.numResults != 0){addPage();}
             $('#content').html("");
             $('#recommendation').html("");
             $('#content').html("<h3><center>Result</center></h3>");
             $('#recommendation').html("<h3><center>Recommendation</center></h3>");
             $('#slogan').html("");
            $.each(data.results,function(i,item){
              info(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance,item.Menu);
            });
            $.each(data.recommend,function(i,item){
              //alert(item.Distance);
              infocommend(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance);
            });
          },
          error: function(XMLHttpeRequest, textStatus, errorThrown){
            alert("Error!");
          }
        });
   });

  $("#Next").click(function(){
        $('#content').html("");
        $('#recommendation').html("");
        var engine = '';
        $('#example-navbar-collapse .active').each(function(){
          engine = $(this).attr('id');
        });
        if (engine == "hadoop-button"){engine = "Hadoop";}
        if (engine == "lucene-button"){engine = "Lucene";}
        var pageNum = $('#currentPageNumber').text();
        var temp = Number(pageNum);
        //if (temp > 1){temp = temp - 1;}
        temp = temp + 1;
        pageNum = String(temp);
        $('#currentPageNumber').text(pageNum);
        var sort = '';
        sort = $('#orderoption').val();
        if(sort == 'Order By Default'){sort = 'Default';}
        if(sort == 'Order By Rate'){sort = 'Rate';}
        if(sort == 'Order By Distance'){sort = 'Distance';}
        var content = $('#query').val();
        var city = $('#city').val();
        //alert(' Content:'+ content + ' City:' + city + 'Engine: ' + engine + 'pagenumber:' + pageNum);
        addPage(pageNum);
        //alert($('#example').text());
        $.ajax({
          url: 'http://localhost:8080/Search/Main',//
          data:{
            pageNum:pageNum,
            indexType:engine,
            keywords:content,
            location:city,
            orderType: sort
          },
          type: "post",
          dataType: "json",
          success:function(data){
             if(data.numResults != 0){addPage();}
             $('#content').html("");
             $('#recommendation').html("");
             $('#content').html("<h3><center>Result</center></h3>");
             $('#recommendation').html("<h3><center>Recommendation</center></h3>");
             $('#slogan').html("");
            $.each(data.results,function(i,item){
              info(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance,item.Menu);
            });
            $.each(data.recommend,function(i,item){
              //alert(item.Distance);
              infocommend(item.Category,item.Address,item.Rate,item.URL,item.Name,item.Price,item.Picture,item.Distance);
            });
          },
          error: function(XMLHttpeRequest, textStatus, errorThrown){
            alert("Error!");
          }
        });
   });

  $("#lucene-button").click(function(){
         $("#hadoop-button").removeClass("active");
         $(this).addClass("active");
  });
  $("#hadoop-button").click(function(){
         $("#lucene-button").removeClass("active");
         $(this).addClass("active");
  });
});