<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <title> Medicament modificat </title>
</head>

<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f2f2f2;">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td align="center" valign="center" bgcolor="#333333"
            style="background-color: #cc0000; height: 50px; ">
            <h3 style="color: #ffffff; "> Pentru pacientul ${patientName} </h3>
        </td>
    </tr>
    <tr style="background-color: #f2f2f2; box-shadow: 0px -10px 10px rgba(0, 0, 0, 0.1);">
        <td align="left" valign="center" bgcolor="#f2f2f3"
            style="background-color: #f2f2f2; padding:20px; box-shadow: 0px -10px 10px rgba(0, 0, 0, 0.1);">
            <p style="font-size: 16px; line-height: 1.5;">
                Modificarea medicamentului:
            </p>
            <div align="left" style="background-color: #e5e5e6; border-left: 4px solid #cc0000; width: 100%; margin-left:20px;">
                <p style="font-size: 16px; line-height: 1.5; margin: 5px;">
                    <strong  style="font-size: 18px"> </strong>
                    <b> Medicament: </b> ${medicine} <br>
                    <b> Doza noua: </b> ${doses} <br>
                    <b> Comentariu: </b> ${comment} <br>
                </p>
            </div>
        </td>
    </tr>
</table>

</body>
</html>
