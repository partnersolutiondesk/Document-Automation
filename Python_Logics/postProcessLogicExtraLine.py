import pandas as pd


def postprocess(filename):
    df = pd.read_csv(filename)
	updated_rows = []

	for index, row in df.iterrows():

		if pd.isna(row['Qty']) and pd.isna(row['Unit Price']) and pd.isna(row['Total Price']):

			updated_rows[-1]['Description'] += ' ' + row['Description']
			updated_rows[-1]['Item No'] += ' ' + row['Item No']

		else:
			prev_qty = row['Qty']

			prev_unit_price = row['Unit Price']

			prev_total_price = row['Total Price']


			updated_rows.append(row)
	updated_df = pd.DataFrame(updated_rows)

	updated_df.to_csv('filename', index=False)
