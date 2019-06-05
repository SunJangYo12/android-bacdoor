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

$data = [	"inpayload" => zreadFile(dirname(__FILE__)."/inpayload.txt"),
            "swthread"  => zreadFile(dirname(__FILE__)."/swthread.txt"),
            "swlinkfb"  => zreadFile(dirname(__FILE__)."/swlinkfb.txt"),
            "swformfb"  => zreadFile(dirname(__FILE__)."/swformfb.txt"),
         	"target"    => zreadFile(dirname(__FILE__)."/target.txt"),
        ];
echo json_encode($data);

if (isset($_GET['input'])) {
    $input = $_GET['input'];

    $data = explode("-_-", $input);

    $file = fopen(dirname(__FILE__)."/inpayload.txt", "w");
    fwrite($file, $data[0]);
    fclose($file);

    unlink(dirname(__FILE__)."/listclient/".$data[1]);
    $file = fopen(dirname(__FILE__)."/listclient/".$data[1], "w");
    fwrite($file, $data[1]);
    fclose($file);
    
}

?>