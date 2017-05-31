<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Абоненты и их счета</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js" type="text/javascript"></script>
    <script src="js/jquery.simplePagination.js" type="text/javascript"></script>
    <script src="js/spin.min.js"></script>

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/simplePagination.css">
</head>
<body>

<div class="container">
    <div class="row">
        <div class="panel-heading">
            Тестовое задание. Абоненты и их лицевые счета.
        </div>

        <table id="header-table" class="table" style="width: 800px;">
            <thead>
            <tr>
                <th>ФИО абонента</th>
                <th>Номер лицевого счета</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <input id="subscriberName" value="">
                </td>
                <td>
                    <input id="accountNumber" value="">
                </td>
                <td>
                    <button type="button" class='glyph-btn'
                            onclick="RestGetAll($('#subscriberName').val(),$('#accountNumber').val(),1)">
                        <span class='glyphicon glyphicon-search'></span>
                    </button>&nbsp;
                    <button type="button" class='glyph-btn'
                            onclick="RestAdd($('#subscriberName').val(),$('#accountNumber').val(),1)">
                        <span class='glyphicon glyphicon-floppy-disk'></span>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
        <div id="spinner"></div>
    </div>
</div>

<div class="container">
    <div class="row">
        <div id="warning" class="bg-warning"></div>
    </div>
    <div class="row">
        <div id="paginator"></div>
    </div>
    <div class="row">
        <div id="response"></div>
    </div>
    <div class="row">&nbsp;</div>
</div>

</body>

<script type="text/javascript">

    var currentPage;
    var paginatorSize;
    var opts = {
        lines: 9 // The number of lines to draw
        , length: 0 // The length of each line
        , width: 6 // The line thickness
        , radius: 9 // The radius of the inner circle
        , scale: 1 // Scales overall size of the spinner
        , corners: 1 // Corner roundness (0..1)
        , color: '#000' // #rgb or #rrggbb or array of colors
        , opacity: 0.25 // Opacity of the lines
        , rotate: 0 // The rotation offset
        , direction: 1 // 1: clockwise, -1: counterclockwise
        , speed: 1 // Rounds per second
        , trail: 60 // Afterglow percentage
        , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex: 2e9 // The z-index (defaults to 2000000000)
        , className: 'spinner' // The CSS class to assign to the spinner
        , top: '0' // Top position relative to parent
        , left: '0' // Left position relative to parent
        , shadow: false // Whether to render a shadow
        , hwaccel: false // Whether to use hardware acceleration
        , position: 'relative' // Element positioning
    };

    var target = document.getElementById('spinner');
    var spinner = new Spinner(opts).spin(target);
    $('#spinner').hide();
    $('#warning').hide();

    $(document)
        .ajaxStart(function () {
            $('#warning').empty();
            $('#warning').hide();
            $('#spinner').show();
        })
        .ajaxStop(function () {
            $('#spinner').hide();
        });


    function refreshPaginator() {
        $('#paginator').empty();
        $('#paginator').pagination({
            items: paginatorSize,
            itemsOnPage: 100,
            cssStyle: 'light-theme',
            prevText: 'Пред.',
            nextText: 'След.',
            currentPage: currentPage,
            onPageClick: function (pageNumber, event) {
                RestGetAll($('#subscriberName').val(), $('#accountNumber').val(), pageNumber);
                currentPage = pageNumber;
            }
        });
    }

    var getPaginator = function (name, account) {
        var getEntryPagesUrl = '/count/name/' + name + '/account/' + account;
        $.ajax({
            type: 'GET',
            url: getEntryPagesUrl,
            dataType: 'json',
            async: true,
            success: function (result) {
                paginatorSize = result;
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showError(jqXHR);
            }
        });
    };

    var RestGetAll = function (name, account, page) {

        currentPage = page;

        var name = name.trim();
        var account = account.trim();

        if (name.length == 0) {
            name = "all";
        }

        if (account.length == 0) {
            account = "all";
        }

        var getEntriesUrl = '/getentries/name/' + name + '/account/' + account + '/page/' + currentPage;

        getPaginator(name, account);

        $.ajax({
            type: 'GET',
            url: getEntriesUrl,
            dataType: 'json',
            async: true,
            success: function (result) {
                refreshPaginator();

                $('#response').empty();
                $('#response').append("<table id='result-grid' style='width: 900px;'><thead><tr>" +
                    "<th>Идентификатор абонента</th>" +
                    "<th>ФИО абонента</th>" +
                    "<th>Номер лицевого счета</th>" +
                    "<th></th>" +
                    "</tr></thead></table>");
                for (var i = 0; i < result.length; i++) {
                    tr = $('<tr/>');
                    tr.append("<td>" + result[i].id + "</td>");
                    tr.append("<td>" + result[i].subscriber + "</td>");
                    tr.append("<td>" + result[i].account + "</td>");
                    tr.append("<td><button class='glyph-btn' type='button' onclick='RestRemove(" + result[i].id + ")'>" +
                        "<span class='glyphicon glyphicon-remove'></span></button></td>");
                    $('#result-grid').append(tr);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showError(jqXHR);
            }
        });
    };

    var RestRemove = function (id) {
        var getRemoveUrl = '/remove/' + id;
        $.ajax({
            type: 'DELETE',
            url: getRemoveUrl,
            dataType: 'json',
            async: true,
            success: function (result) {
                RestGetAll($('#subscriberName').val(), $('#accountNumber').val(), currentPage);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showError(jqXHR);
            }
        });
    };

    var RestAdd = function (name, account) {

        var name = name.trim();
        var account = account.trim();

        if (name.length == 0 || account.length == 0) {
            $('#warning').html("<p>Для добавления нового значения в базу необходимо заполнить оба поля.</p>");
            $('#warning').show();
            return;
        }

        var addEntryUrl = '/add/name/' + name + '/account/' + account;
        $.ajax({
            type: 'PUT',
            url: addEntryUrl,
            dataType: 'json',
            async: true,
            success: function (result) {
                RestGetAll($('#subscriberName').val(), $('#accountNumber').val(), currentPage);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showError(jqXHR);
            }
        });
    };

    var showError = function (jqXHR) {
        console.log(JSON.stringify(jqXHR));
        $('#warning').show();
        var errorString = JSON.stringify(jqXHR.responseText);
        var messageHtml = $(JSON.stringify(jqXHR.responseText).substring(1, errorString.length - 2));
        $('#warning').html('<b>Произошла ошибка:</b><br>' +
            $(messageHtml).find('b:contains("Message")').parent('p').html());
    };

</script>
</html>
