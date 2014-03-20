<html>
    <head>
        <style type="text/css">
            * {font-family: 'Segoe UI';}
            .tabbable .tabs {list-style: none; margin: 0 10px; padding: 0;}
            .tabbable .tabs li {list-style: none; margin: 0; padding: 0; display: inline-block; position: relative; z-index: 1;}
            .tabbable .tabs li a {text-decoration: none; color: #000; border: 1px solid #ccc; padding: 5px; display: inline-block; border-radius: 5px 5px 0 0; background: #f5f5f5;}
            .tabbable .tabs li a.active, .tabbable .tabs li a:hover {border-bottom-color: #fff; background: #fff;}
            .tabcontent {border: 1px solid #ccc; margin-top: -1px; padding: 10px;}
        </style>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js">
        </script>
        <script>
            $(document).ready(function(){
                $(".tabbable").find(".tab").hide();
                $(".tabbable").find(".tab").first().show();
                $(".tabbable").find(".tabs li").first().find("a").addClass("active");
                $(".tabbable").find(".tabs").find("a").click(function(){
                    tab = $(this).attr("href");
                    $(".tabbable").find(".tab").hide();
                    $(".tabbable").find(".tabs").find("a").removeClass("active");
                    $(tab).show();
                    $(this).addClass("active");
                    return false;
                });

                $('#registerForm').submit(function(event){
                    var email = $('#email').val();
                    var password = $('#password').val();
                    var VIPCode = $('#VIPCode').val();
                    $.ajax({
                        url: $('#registerForm').attr("action"),
                        data: "email=" + email + "&password=" + password + "&VIPCode=" + VIPCode,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#loginForm').submit(function(event){
                    var email = $('#loginEmail').val();
                    var password = $('#loginPassword').val();
                    $.ajax({
                        url: $('#loginForm').attr("action"),
                        data: "email=" + email + "&password=" + password,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#refreshAccessTokenForm').submit(function(event){
                    var uid = $('#refreshAccessTokenUid').val();
                    var refreshToken = $('#refreshAccessTokenRefreshToken').val();
                    $.ajax({
                        url: $('#refreshAccessTokenForm').attr("action"),
                        data: "uid=" + uid + "&refreshToken=" + refreshToken,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#sendMessageForm').submit(function(event){
                    var uid = $('#sendMessageSenderUid').val();
                    var accessToken = $('#sendMessageSenderAccessToken').val();
                    var receiverUid = $('#sendMessageReceiverUid').val();
                    var message = $('#sendMessageMessage').val();
                    var jsonString = "{\"uid\":\"" + uid + "\",\"accessToken\":\""+ accessToken
                        + "\",\"receiverUid\":\"" + receiverUid + "\",\"message\":\"" + message + "\"}";
                    console.log(jsonString);
                    $.ajax({
                        url: $('#sendMessageForm').attr("action"),
                        data: "jsonString=" + jsonString,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#fileUploadForm').submit(function(event){
                    var formData = new FormData($(this)[0]);
                    console.log(formData);
                    $.ajax({
                        url: $('#fileUploadForm').attr("action"),
                        data: formData,
                        type: "POST",
                        async: false,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#setGenderForm').submit(function(event){
                    var uid = $('#setGenderUid').val();
                    var accessToken = $('#setGenderAccessToken').val();
                    var gender = $('[name="setGenderGender"]').val();
                    $.ajax({
                        url: $('#setGenderForm').attr("action"),
                        data: "uid=" + uid + "&accessToken=" + accessToken + "&gender=" + gender,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#setUsernameForm').submit(function(event){
                    var uid = $('#setUsernameUid').val();
                    var accessToken = $('#setUsernameAccessToken').val();
                    var username = $('#setUsernameUsername').val();
                    $.ajax({
                        url: $('#setUsernameForm').attr("action"),
                        data: "uid=" + uid + "&accessToken=" + accessToken + "&username=" + username,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
                $('#getUserAccountForm').submit(function(event){
                    var uid = $('#getUserAccountUid').val();
                    var accessToken = $('#getUserAccountAccessToken').val();
                    $.ajax({
                        url: $('#getUserAccountForm').attr("action"),
                        data: "uid=" + uid + "&accessToken=" + accessToken,
                        type: "POST",
                        success: function(result){
                            console.log(JSON.stringify(result));
                            $('#serviceResponse').html(JSON.stringify(result));
                        }
                    });
                    event.preventDefault();
                });
            });
        </script>
        <title>Web Services</title>
    </head>
    <body>
        <h2>Services Console</h2>
        <div class="tabbable">
            <ul class="tabs">
                <li><a href="#tab1">Register</a></li>
                <li><a href="#tab2">Login</a></li>
                <li><a href="#tab3">Refresh accessToken</a></li>
                <li><a href="#tab4">Send message</a></li>
                <li><a href="#tab5">Upload files</a></li>
                <li><a href="#tab6">Download files</a></li>
                <li><a href="#tab7">Set Gender</a></li>
                <li><a href="#tab8">Set Username</a></li>
                <li><a href="#tab9">Account Info</a></li>
            </ul>
            <div class="tabcontent">
                <div id="tab1" class="tab">
                    <form id="registerForm" method="POST" action="/ServicesConsole/register">
                        <table>
                            <tr>
                                <td>Email</td>
                                <td>
                                    <input id="email" />
                                </td>
                            </tr>
                            <tr>
                                <td>Password</td>
                                <td>
                                    <input id="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>VIP Code</td>
                                <td>
                                    <input id="VIPCode" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab2" class="tab">
                    <form id="loginForm" method="POST" action="/ServicesConsole/login">
                        <table>
                            <tr>
                                <td>Email:</td>
                                <td>
                                    <input id="loginEmail" />
                                </td>
                            </tr>
                            <tr>
                                <td>Password:</td>
                                <td>
                                    <input id="loginPassword" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab3" class="tab">
                    <form id="refreshAccessTokenForm" method="POST" action="/ServicesConsole/refreshAccessToken">
                        <table>
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input id="refreshAccessTokenUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Refresh Token:</td>
                                <td>
                                    <input id="refreshAccessTokenRefreshToken" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab4" class="tab">
                    <form id="sendMessageForm" method="POST" action="/ServicesConsole/sendMessage">
                        <table>
                            <tr>
                                <td>Sender UID:</td>
                                <td>
                                    <input id="sendMessageSenderUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input id="sendMessageSenderAccessToken" />
                                </td>
                            </tr>
                            <tr>
                                <td>Receiver UID:</td>
                                <td>
                                    <input id="sendMessageReceiverUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Message Body:</td>
                                <td>
                                    <input id="sendMessageMessage" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                 <div id="tab5" class="tab">
                    <form id="fileUploadForm" modelAttribute="uploadForm" method="POST" action="/ServicesConsole/fileUpload" enctype="multipart/form-data">
                        <table id="fileTable">
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input name="uid" type="text"/>
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input name="accessToken" type="text"/>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="file" type="file" /></td>
                            </tr>
                            <tr>
                                <td>
                                    Usage:
                                    <select name="imageUsage">
                                        <option value="forConversation" selected>For Conversation</option>
                                        <option value="forPost">For Post</option>
                                        <option value="forProfile">For Profile</option>
                                    </select>
                                </td>
                            </tr>
                        </table>
                        <br/><input type="submit" value="Upload" />
                    </form>
                 </div>
                 <div id="tab6" class="tab">
                    <form id="fileDownloadForm" method="POST" action="/ServicesConsole/fileDownload">
                        <table>
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input name="fileDownloadUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input name="fileDownloadAccessToken" />
                                </td>
                            </tr>
                            <tr>
                                <td>File Name:</td>
                                <td>
                                    <input name="fileDownloadFilename" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab7" class="tab">
                    <form id="setGenderForm" method="POST" action="/ServicesConsole/setGender">
                        <table>
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input id="setGenderUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input id="setGenderAccessToken" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                Gender:
                                    <select name="setGenderGender">
                                        <option value="M" selected>Male</option>
                                        <option value="F" selected>Female</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab8" class="tab">
                    <form id="setUsernameForm" method="POST" action="/ServicesConsole/setUsername">
                        <table>
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input id="setUsernameUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input id="setUsernameAccessToken" />
                                </td>
                            </tr>
                            <tr>
                                <td>Username:</td>
                                <td>
                                    <input id="setUsernameUsername" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <div id="tab9" class="tab">
                    <form id="getUserAccountForm" method="POST" action="/ServicesConsole/getUserAccount">
                        <table>
                            <tr>
                                <td>UID:</td>
                                <td>
                                    <input id="getUserAccountUid" />
                                </td>
                            </tr>
                            <tr>
                                <td>Access Token:</td>
                                <td>
                                    <input id="getUserAccountAccessToken" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
            </div>
        </div>
        <div id="serviceResponse"></div>
    </body>
</html>