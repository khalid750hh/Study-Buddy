const express = require("express");
const app = express();
const { resolve } = require("path");
// This is our test API key.
const stripe = require("stripe")('sk_test_51JwUnKCK34XwtTfpx8imoFRh6UoRbfJ5hRM8elFzR0sL2BtXfNc1FXidbhtVfI5Of3k8kwhcKHmvvam4Uq3jk1xL00QKbRDD8n');
app.use(express.static("."));
app.use(express.json());

const calculateOrderAmount = items => {
  // Replace this constant with a calculation of the order's amount
  // Calculate the order total on the server to prevent
  // people from directly manipulating the amount on the client
  console.log(items[0].amount)
  return items[0].amount;
};

app.post("/create-payment-intent", async (req, res) => {
  const { items } = req.body;
  const { currency } = req.body;

  // Create a PaymentIntent with the order amount and currency
  const paymentIntent = await stripe.paymentIntents.create({
    amount: calculateOrderAmount(items),
    currency: currency
  });

  res.send({
    clientSecret: paymentIntent.client_secret
  });
});
app.get("/greet", async (req, res) => {
    res.send('hello, it is working!');
});

app.listen(4242, () => console.log("Node server listening on port 4242!"));