<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <title>Activare cont pacient</title>
</head>

<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f2f2f2;">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td align="center" valign="center" bgcolor="#333333"
            style="background-color: #000099; height: 50px; ">
            <h3 style="color: #ffffff; ">Resetare parola cont <span>${companyName}</span> </h3>
        </td>
    </tr>
    <tr style="background-color: #f2f2f2; box-shadow: 0px -10px 10px rgba(0, 0, 0, 0.1);">
        <td align="left" valign="center" bgcolor="#f2f2f3"
            style="background-color: #f2f2f2; padding:20px; box-shadow: 0px -10px 10px rgba(0, 0, 0, 0.1);">
            <p style="font-size: 16px; line-height: 1.5;">
                Cererea ta pentru <i> resetarea parolei </i> a fost primita. Folositi <i> aceste credentiale </i> si alegeti-va <i> o parola noua </i> pentru contul dvs :
            </p>
            <div align="left" style="background-color: #e5e5e6; border-left: 4px solid #000099; width: 100%; margin-left:20px;">
                <p style="font-size: 16px; line-height: 1.5; margin: 5px;">
                    <strong  style="font-size: 18px"> </strong>
                    <b>Email:</b> ${email}<br>
                    <b>Parola temporara:</b> ${password}
                </p>
            </div>

            <p style="font-size: 16px;"> Pentru a face acest lucru, accesati urmatorul link: ${link} </a> </p>
            <p style="font-size: 16px; line-height: 1.5;">
                Toate cele bune,<br>
                <span style="font-style: italic; color: #000066"><b>${companyName}</b></span>
            </p>
        </td>
    </tr>
</table>

</body>
</html>
