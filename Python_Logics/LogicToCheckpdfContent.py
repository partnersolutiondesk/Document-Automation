import PyPDF4


def pdfLogictoSeparateDoc(filepath):

    with open(filepath,'rb') as file:
        reader = PyPDF4.PdfFileReader(file)

        page = reader.getPage(2)

        text = page.extractText()

        lines = text.splitlines()

        if "Page " in lines and ("Water" in lines[lines.index("Page ")+1] or "Electricity" in lines[lines.index("Page ")+1])\
                and ("CONSUMPTION" in lines[lines.index("Page ")+2]):
            print("true")
        else:
            print("false")

# print(pdfLogictoSeparateDoc('C:\\Users\\syed.hasnain\\Downloads\\Splited bills\\25611218115_1.pdf'))