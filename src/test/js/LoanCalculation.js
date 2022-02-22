function calculate(amount, annualInterest, tenureMonths) {

   const principal = amount;
   const interest = annualInterest / 100 / 12;
   const payments = tenureMonths * 12;

   const x = Math.pow(1 + interest, payments);
   const monthly = (principal * x * interest) / (x - 1);
   if (isFinite(monthly)) {
      return({
         monthlyPayment: monthly.toFixed(2),
         totalAmount: (monthly * payments).toFixed(2),
         totalInterestAmount: ((monthly*payments)-principal).toFixed(2)
      });
   } else {
      throw("Result was Not-a-Number or infinite");
   }
}

let calculation = calculate(60000, 3.55, 10);
print("Monthly payment = " + calculation.monthlyPayment);
print("Total amount = " + calculation.totalAmount);
print("Total interest = " + calculation.totalInterestAmount)